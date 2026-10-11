import type { PoolClient } from 'pg';
import { z } from 'zod';
import type { RequestContext } from '../../platform/request-context/context';
import { AppError, ErrorCode } from '../../platform/errors/errors';
import { insertAudit, insertDomainEventAndOutbox } from '../../platform/events/outbox';
import { assertCompanyMomentAccess } from './membership';
import {
  canApprove,
  canSubmit,
  familyForSubtype,
  isLegacyBusinessFamily,
  isOperatingFamily,
  isOperatingSubtype,
  needsMakerChecker,
  normalizeOperatingPreferences,
  projectOperatingMoment,
  quickAddDef,
  resolveRole,
  type OperatingEventRow,
  type OperatingPreferences,
  type OperatingProjection,
  type OperatingRole,
} from './operating-catalog';

const amountSchema = z.string().regex(/^\d+(\.\d{1,4})?$/);

export const recordOperatingEventSchema = z
  .object({
    eventType: z.string().min(1).max(40),
    title: z.string().min(1).max(240),
    note: z.string().max(2000).optional(),
    amount: amountSchema.optional(),
    counterpartyName: z.string().max(200).optional(),
    channelName: z.string().max(120).optional(),
    paymentStatus: z.enum(['NONE', 'PENDING', 'READY', 'PAID', 'COLLECTED']).optional(),
  })
  .strict();

export const decideOperatingEventSchema = z
  .object({
    decision: z.enum(['APPROVED', 'REJECTED']),
    reason: z.string().max(500).optional(),
  })
  .strict();

export type RecordOperatingEventInput = z.infer<typeof recordOperatingEventSchema>;
export type DecideOperatingEventInput = z.infer<typeof decideOperatingEventSchema>;

interface SetupRow {
  family_code: string;
  title: string;
  preferences: OperatingPreferences;
  company_id: string;
}

async function loadSetup(client: PoolClient, momentId: string): Promise<SetupRow | null> {
  const row = await client.query<SetupRow>(
    `SELECT family_code, title, preferences, company_id
     FROM business.business_system_setup
     WHERE moment_id = $1 AND status = 'ACTIVE'
     ORDER BY updated_at DESC
     LIMIT 1`,
    [momentId]
  );
  return row.rows[0] ?? null;
}

async function loadEvents(client: PoolClient, momentId: string): Promise<OperatingEventRow[]> {
  const rows = await client.query<{
    operating_event_id: string;
    event_type: string;
    title: string;
    note: string | null;
    amount: string | null;
    currency_code: string | null;
    counterparty_name: string | null;
    channel_name: string | null;
    payment_relevance: string;
    payment_status: string;
    approval_status: string;
    submitted_by_user_id: string;
    decided_by_user_id: string | null;
    occurred_at: Date;
  }>(
    `SELECT operating_event_id, event_type, title, note, amount::text, currency_code,
            counterparty_name, channel_name, payment_relevance, payment_status, approval_status,
            submitted_by_user_id, decided_by_user_id, occurred_at
     FROM business.operating_event
     WHERE moment_id = $1
     ORDER BY occurred_at DESC
     LIMIT 200`,
    [momentId]
  );
  return rows.rows.map((r) => ({
    operatingEventId: r.operating_event_id,
    eventType: r.event_type,
    title: r.title,
    note: r.note,
    amount: r.amount,
    currencyCode: r.currency_code,
    counterpartyName: r.counterparty_name,
    channelName: r.channel_name,
    paymentRelevance: r.payment_relevance,
    paymentStatus: r.payment_status,
    approvalStatus: r.approval_status,
    submittedByUserId: r.submitted_by_user_id,
    decidedByUserId: r.decided_by_user_id,
    occurredAt: r.occurred_at.toISOString(),
  }));
}

function prefsOf(raw: OperatingPreferences | Record<string, unknown>): OperatingPreferences {
  return raw as OperatingPreferences;
}

export async function viewerRole(
  client: PoolClient,
  userId: string,
  momentId: string,
  companyId: string | null,
  prefs: OperatingPreferences
): Promise<OperatingRole> {
  if (!companyId) {
    return resolveRole(prefs.participants ?? [], userId, 'MEMBER');
  }
  const membership = await client.query<{ membership_type: string }>(
    `SELECT membership_type FROM business.company_membership
     WHERE company_id = $1 AND user_id = $2 AND status = 'ACTIVE'`,
    [companyId, userId]
  );
  return resolveRole(prefs.participants ?? [], userId, membership.rows[0]?.membership_type ?? 'MEMBER');
}

export async function getOperatingProjection(
  client: PoolClient,
  ctx: RequestContext,
  momentId: string
): Promise<OperatingProjection | { retired: true; familyCode: string; title: string }> {
  const scope = await assertCompanyMomentAccess(client, ctx, momentId);
  if (isLegacyBusinessFamily(scope.businessFamily) || !isOperatingFamily(scope.businessFamily)) {
    const title = await client.query<{ title: string }>(
      `SELECT title FROM core.moment WHERE moment_id = $1`,
      [momentId]
    );
    return {
      retired: true,
      familyCode: scope.businessFamily,
      title: title.rows[0]?.title ?? 'Business moment',
    };
  }
  const setup = await loadSetup(client, momentId);
  const prefs = setup ? prefsOf(setup.preferences) : null;
  if (!prefs?.subtype) {
    return { retired: true, familyCode: scope.businessFamily, title: 'Business moment' };
  }
  const role = await viewerRole(client, ctx.userId, momentId, scope.companyId, prefs);
  const events = await loadEvents(client, momentId);
  return projectOperatingMoment({
    familyCode: scope.businessFamily,
    prefs,
    role,
    events,
  });
}

export async function recordOperatingEvent(
  client: PoolClient,
  ctx: RequestContext,
  momentId: string,
  body: RecordOperatingEventInput
): Promise<{ eventId: string; approvalStatus: string; projection: OperatingProjection }> {
  const scope = await assertCompanyMomentAccess(client, ctx, momentId);
  if (!isOperatingFamily(scope.businessFamily)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Quick Add is only available on an operating moment.', 400);
  }
  const setup = await loadSetup(client, momentId);
  if (!setup) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Operating setup was not found.', 400);
  }
  const prefs = prefsOf(setup.preferences);
  const def = quickAddDef(body.eventType);
  if (!def || !familyForSubtype(prefs.subtype)?.subtypes.find((s) => s.code === prefs.subtype)?.quickAdds.includes(def.code)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, `This moment does not include ${body.eventType}.`, 400);
  }
  const role = await viewerRole(client, ctx.userId, momentId, scope.companyId, prefs);
  if (!canSubmit(role, def)) {
    throw new AppError(ErrorCode.GOVERNANCE_DENIED, 'Your role cannot add this.', 403);
  }
  if (def.needsAmount && !body.amount && def.code !== 'UPDATE') {
    if (def.consumesBudget || def.countsTarget || def.code === 'BUDGET' || def.code === 'TARGET' || def.code === 'PAYMENT' || def.code === 'COLLECTION') {
      throw new AppError(ErrorCode.VALIDATION_FAILED, 'Amount is required.', 400);
    }
  }
  const checker = needsMakerChecker(role, def);
  const approvalStatus = checker || def.code === 'APPROVAL' ? 'PENDING' : 'NONE';
  let paymentStatus = body.paymentStatus ?? 'NONE';
  if (def.paymentRelevance !== 'NONE' && paymentStatus === 'NONE') {
    paymentStatus = 'PENDING';
  }
  if (def.paymentRelevance === 'NONE') paymentStatus = 'NONE';
  const inserted = await client.query<{ operating_event_id: string }>(
    `INSERT INTO business.operating_event (
       moment_id, company_id, actor_user_id, event_type, title, note, amount, currency_code,
       counterparty_name, channel_name, payment_relevance, payment_status, approval_status,
       submitted_by_user_id
     ) VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$3)
     RETURNING operating_event_id`,
    [
      momentId,
      scope.companyId,
      ctx.userId,
      def.code,
      body.title,
      body.note ?? null,
      body.amount ?? null,
      prefs.currency,
      body.counterpartyName ?? null,
      body.channelName ?? null,
      def.paymentRelevance,
      paymentStatus,
      approvalStatus,
    ]
  );
  const eventId = inserted.rows[0]!.operating_event_id;
  if (approvalStatus !== 'PENDING' && (def.code === 'BUDGET' || def.code === 'TARGET') && body.amount) {
    await applyPlanRevision(client, momentId, def.code, body.amount);
  }
  const { domainEventId } = await insertDomainEventAndOutbox(client, ctx, {
    eventName: 'OperatingEventRecorded',
    domainCode: 'BUSINESS',
    aggregateType: 'OPERATING_EVENT',
    aggregateId: eventId,
    scopeType: 'MOMENT',
    scopeId: momentId,
    payload: { eventId, eventType: def.code, momentId, approvalStatus },
  });
  await insertAudit(client, ctx, 'OPERATING_EVENT_RECORD', 'OPERATING_EVENT', eventId, domainEventId, {
    momentId,
    eventType: def.code,
    approvalStatus,
  });
  const projection = await getOperatingProjection(client, ctx, momentId);
  if ('retired' in projection && projection.retired) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Operating projection unavailable.', 400);
  }
  return { eventId, approvalStatus, projection: projection as OperatingProjection };
}

async function applyPlanRevision(
  client: PoolClient,
  momentId: string,
  code: 'BUDGET' | 'TARGET',
  amount: string
): Promise<void> {
  const key = code === 'BUDGET' ? 'monthlyBudget' : 'monthlyTarget';
  await client.query(
    `UPDATE business.business_system_setup
     SET preferences = jsonb_set(preferences, $2::text[], to_jsonb($3::text), true),
         version = version + 1,
         updated_at = now()
     WHERE moment_id = $1 AND status = 'ACTIVE'`,
    [momentId, `{${key}}`, amount]
  );
}

export async function decideOperatingEvent(
  client: PoolClient,
  ctx: RequestContext,
  momentId: string,
  eventId: string,
  body: DecideOperatingEventInput
): Promise<{ eventId: string; approvalStatus: string }> {
  const scope = await assertCompanyMomentAccess(client, ctx, momentId);
  const setup = await loadSetup(client, momentId);
  if (!setup || !isOperatingFamily(scope.businessFamily)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Operating setup was not found.', 400);
  }
  const prefs = prefsOf(setup.preferences);
  const role = await viewerRole(client, ctx.userId, momentId, scope.companyId, prefs);
  if (!canApprove(role)) {
    throw new AppError(ErrorCode.GOVERNANCE_DENIED, 'Your role cannot approve this.', 403);
  }
  const existing = await client.query<{
    submitted_by_user_id: string;
    approval_status: string;
    event_type: string;
    amount: string | null;
  }>(
    `SELECT submitted_by_user_id, approval_status, event_type, amount::text
     FROM business.operating_event
     WHERE operating_event_id = $1 AND moment_id = $2`,
    [eventId, momentId]
  );
  const row = existing.rows[0];
  if (!row) throw new AppError(ErrorCode.VALIDATION_FAILED, 'Event was not found.', 404);
  if (row.approval_status !== 'PENDING') {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'This event is not waiting for approval.', 400);
  }
  if (row.submitted_by_user_id === ctx.userId) {
    throw new AppError(ErrorCode.GOVERNANCE_DENIED, 'You cannot approve an action you submitted.', 403);
  }
  await client.query(
    `UPDATE business.operating_event
     SET approval_status = $2, decided_by_user_id = $3, version = version + 1
     WHERE operating_event_id = $1`,
    [eventId, body.decision, ctx.userId]
  );
  if (body.decision === 'APPROVED' && (row.event_type === 'BUDGET' || row.event_type === 'TARGET') && row.amount) {
    await applyPlanRevision(client, momentId, row.event_type, row.amount);
  }
  const { domainEventId } = await insertDomainEventAndOutbox(client, ctx, {
    eventName: 'OperatingEventDecided',
    domainCode: 'BUSINESS',
    aggregateType: 'OPERATING_EVENT',
    aggregateId: eventId,
    scopeType: 'MOMENT',
    scopeId: momentId,
    payload: { eventId, decision: body.decision, momentId },
  });
  await insertAudit(client, ctx, 'OPERATING_EVENT_DECIDE', 'OPERATING_EVENT', eventId, domainEventId, {
    decision: body.decision,
    reason: body.reason ?? null,
  });
  return { eventId, approvalStatus: body.decision };
}

export function assertOperatingActivate(familyCode: string): void {
  if (isLegacyBusinessFamily(familyCode)) {
    throw new AppError(
      ErrorCode.VALIDATION_FAILED,
      'Team & Work, Money & Cash Flow, and Daily Business are retired. Create a Marketing, Trading, Retail, or Manufacturing moment.',
      400
    );
  }
  if (!isOperatingFamily(familyCode)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, `Unknown business setup: ${familyCode}`, 400);
  }
}

export function momentTypeForActivate(familyCode: string, requested: string | undefined, subtype: string): string {
  const code = (requested ?? subtype).toUpperCase();
  if (!isOperatingSubtype(code) || familyForSubtype(code)?.familyCode !== familyCode) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Subtype does not belong to this moment.', 400);
  }
  return code;
}

export { normalizeOperatingPreferences };
