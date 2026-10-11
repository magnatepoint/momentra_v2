import type { PoolClient } from 'pg';
import { z } from 'zod';
import type { RequestContext } from '../../platform/request-context/context';
import { AppError, ErrorCode } from '../../platform/errors/errors';
import { insertAudit, insertDomainEventAndOutbox } from '../../platform/events/outbox';
import { createMoment } from '../moment/service';
import { derivePaymentMethodFromAccountType, getAccountType, resolveUserAccount } from '../finance/financial-account';
import { storeCompletedMedia } from '../media/service';
import {
  NON_ECONOMIC_KINDS,
  projectPersonalExperience,
  type LifeExpenseFact,
  type PersonalExperience,
} from './experience-projection';
import {
  getPersonalLifeArea,
  getPersonalLifeSubcategory,
  isPersonalMoodCode,
  isValidPersonalLifeContext,
  personalLifeLayerOf,
} from './taxonomy.generated';

const moodSchema = z.enum(['GREAT', 'GOOD', 'OKAY', 'UNEASY', 'BAD']);
const classificationSchema = z.enum(['manual', 'suggested', 'inferred', 'corrected']);
const MAX_PHOTO_BYTES = 6 * 1024 * 1024;

export const personalExpensePhotoSchema = z
  .object({
    contentType: z.enum(['image/jpeg', 'image/png', 'image/webp']),
    dataBase64: z.string().min(1).max(8_000_000),
  })
  .strict();

export type PersonalExpensePhotoInput = z.infer<typeof personalExpensePhotoSchema>;

export const createPersonalLifeExpenseSchema = z
  .object({
    amount: z.string().regex(/^\d+(\.\d{1,4})?$/),
    currencyCode: z.string().regex(/^[A-Za-z]{3}$/),
    merchantName: z.string().max(500).optional(),
    effectiveAt: z.string().refine((s) => !Number.isNaN(Date.parse(s)), { message: 'Invalid ISO datetime' }).optional(),
    note: z.string().max(2000).optional(),
    lifeAreaCode: z.string().regex(/^P[1-8]$/),
    lifeSubcategoryCode: z.string().regex(/^P[1-8]\.[1-6]$/),
    moodCode: moodSchema.nullable().optional(),
    classificationSource: classificationSchema.optional(),
    origin: z.string().max(40).optional(),
    paymentSource: z.string().max(80).optional(),
    economicKind: z.string().max(40).optional(),
    photo: personalExpensePhotoSchema.nullish(),
  })
  .strict();

export type CreatePersonalLifeExpenseInput = z.infer<typeof createPersonalLifeExpenseSchema>;

export const updatePersonalLifeExpenseSchema = z
  .object({
    lifeAreaCode: z.string().regex(/^P[1-8]$/),
    lifeSubcategoryCode: z.string().regex(/^P[1-8]\.[1-6]$/),
    moodCode: moodSchema.nullable().optional(),
    merchantName: z.string().max(500).nullable().optional(),
    note: z.string().max(2000).nullable().optional(),
    photo: personalExpensePhotoSchema.nullish(),
  })
  .strict();

export type UpdatePersonalLifeExpenseInput = z.infer<typeof updatePersonalLifeExpenseSchema>;

export const refundPersonalLifeExpenseSchema = z
  .object({
    amount: z.string().regex(/^\d+(\.\d{1,4})?$/).optional(),
  })
  .strict();

function assertContext(areaCode: string, subcategoryCode: string, moodCode: string | null | undefined): void {
  if (!isValidPersonalLifeContext(areaCode, subcategoryCode)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Subcategory does not belong to the selected Life Area.', 400);
  }
  if (moodCode != null && !isPersonalMoodCode(moodCode)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Mood must be one of Great, Good, Okay, Uneasy, Bad.', 400);
  }
}

function rejectNonEconomic(kind: string | undefined): void {
  if (kind && (NON_ECONOMIC_KINDS as readonly string[]).includes(kind)) {
    throw new AppError(
      ErrorCode.VALIDATION_FAILED,
      'Transfers, settlements, wallet loads, and cash withdrawals are not personal expenses.',
      400
    );
  }
  if (kind && kind !== 'EXPENSE') {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Only an economic expense can be recorded here.', 400);
  }
}

async function ensurePersonalLedger(client: PoolClient, ctx: RequestContext): Promise<string> {
  const existing = await client.query<{ moment_id: string }>(
    `SELECT pmc.moment_id
     FROM personal.personal_moment_context pmc
     JOIN core.moment m ON m.moment_id = pmc.moment_id
     WHERE pmc.user_id = $1 AND pmc.status = 'ACTIVE' AND m.domain_code = 'PERSONAL'
     ORDER BY (pmc.personal_context_type = 'LEDGER') DESC, pmc.created_at ASC
     LIMIT 1`,
    [ctx.userId]
  );
  if (existing.rows[0]) return existing.rows[0].moment_id;

  const created = await createMoment(client, ctx, {
    domainCode: 'PERSONAL',
    momentTypeCode: 'LIFE_RHYTHM',
    title: 'Personal',
    timezone: 'UTC',
    status: 'ACTIVE',
  });
  await client.query(
    `UPDATE personal.personal_moment_context
     SET personal_context_type = 'LEDGER', updated_at = now()
     WHERE moment_id = $1 AND user_id = $2`,
    [created.momentId, ctx.userId]
  );
  return created.momentId;
}

async function loadFacts(client: PoolClient, userId: string): Promise<LifeExpenseFact[]> {
  const rows = await client.query<{
    expense_id: string;
    amount: string;
    currency_code: string;
    merchant_name: string | null;
    note: string | null;
    life_area_code: string;
    life_subcategory_code: string;
    mood_code: string | null;
    classification_source: string | null;
    effective_at: Date;
    status: string;
    origin: string | null;
    economic_kind: 'EXPENSE' | 'REFUND';
    reversed_expense_id: string | null;
  }>(
    `SELECT expense_id, amount::text AS amount, currency_code, merchant_name, note,
            life_area_code, life_subcategory_code, mood_code, classification_source,
            effective_at, status, origin, economic_kind, reversed_expense_id
     FROM finance.expense
     WHERE created_by_user_id = $1
       AND domain_code = 'PERSONAL'
       AND life_area_code IS NOT NULL
       AND life_subcategory_code IS NOT NULL`,
    [userId]
  );
  return rows.rows.map((r) => ({
    expenseId: r.expense_id,
    amount: Number(r.amount),
    currencyCode: r.currency_code,
    merchantName: r.merchant_name,
    note: r.note,
    areaCode: r.life_area_code,
    subcategoryCode: r.life_subcategory_code,
    moodCode: r.mood_code,
    classificationSource: r.classification_source,
    effectiveAt: r.effective_at.toISOString(),
    status: r.status,
    origin: r.origin,
    economicKind: r.economic_kind,
    reversedExpenseId: r.reversed_expense_id,
  }));
}

export async function getPersonalExperience(client: PoolClient, userId: string): Promise<PersonalExperience> {
  return projectPersonalExperience(await loadFacts(client, userId));
}

export interface PersonalExpenseConfirmation {
  expenseId: string;
  momentId: string;
  amount: string;
  currencyCode: string;
  merchantName: string | null;
  effectiveAt: string;
  lifeAreaCode: string;
  lifeAreaLabel: string;
  lifeSubcategoryCode: string;
  lifeSubcategoryLabel: string;
  layer: string;
  moodCode: string | null;
  moodLabel: string | null;
  monthTotal: string;
  monthExpenseCount: number;
  areaMonthTotal: string;
  areaMonthCount: number;
  moodResponses: number;
  moodCoverage: number | null;
  version: number;
  photoUploadId: string | null;
}

function confirmationFrom(
  experience: PersonalExperience,
  saved: {
    expenseId: string;
    momentId: string;
    amount: string;
    currencyCode: string;
    merchantName: string | null;
    effectiveAt: string;
    lifeAreaCode: string;
    lifeSubcategoryCode: string;
    moodCode: string | null;
    version: number;
    photoUploadId?: string | null;
  }
): PersonalExpenseConfirmation {
  const area = experience.life.areas.find((a) => a.areaCode === saved.lifeAreaCode);
  return {
    ...saved,
    lifeAreaLabel: getPersonalLifeArea(saved.lifeAreaCode)?.label ?? saved.lifeAreaCode,
    lifeSubcategoryLabel: getPersonalLifeSubcategory(saved.lifeSubcategoryCode)?.label ?? saved.lifeSubcategoryCode,
    layer: personalLifeLayerOf(saved.lifeAreaCode) ?? 'NOW',
    moodLabel: saved.moodCode ? (experience.pulse.mood.counts.find((c) => c.moodCode === saved.moodCode)?.label ?? null) : null,
    monthTotal: experience.pulse.snapshot.total,
    monthExpenseCount: experience.pulse.snapshot.expenseCount,
    areaMonthTotal: area?.spend ?? '0.00',
    areaMonthCount: area?.activityCount ?? 0,
    moodResponses: experience.pulse.mood.responseCount,
    moodCoverage: experience.pulse.mood.coverage,
    photoUploadId: saved.photoUploadId ?? null,
  };
}

export function decodePersonalExpensePhoto(photo: PersonalExpensePhotoInput): Buffer {
  const cleaned = photo.dataBase64.replace(/\s/g, '');
  if (!/^[A-Za-z0-9+/]+={0,2}$/.test(cleaned)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Photo must be base64-encoded image data.', 400);
  }
  const bytes = Buffer.from(cleaned, 'base64');
  if (bytes.length === 0 || bytes.length > MAX_PHOTO_BYTES) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Photo must be a JPEG, PNG, or WebP under 6 MB.', 400);
  }
  if (!photoMatchesType(bytes, photo.contentType)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Photo bytes do not match the declared content type.', 400);
  }
  return bytes;
}

function photoMatchesType(bytes: Buffer, contentType: string): boolean {
  if (contentType === 'image/jpeg') {
    return bytes.length >= 3 && bytes[0] === 0xff && bytes[1] === 0xd8 && bytes[2] === 0xff;
  }
  if (contentType === 'image/png') {
    return bytes.length >= 8 && bytes.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]));
  }
  return (
    bytes.length >= 12 &&
    bytes.toString('ascii', 0, 4) === 'RIFF' &&
    bytes.toString('ascii', 8, 12) === 'WEBP'
  );
}

async function linkExpensePhoto(
  client: PoolClient,
  ctx: RequestContext,
  expenseId: string,
  momentId: string,
  photo: PersonalExpensePhotoInput
): Promise<string> {
  const bytes = decodePersonalExpensePhoto(photo);
  const stored = await storeCompletedMedia(client, ctx, {
    contentType: photo.contentType,
    bytes,
    scopeType: 'MOMENT',
    scopeId: momentId,
  });
  await client.query(
    `INSERT INTO finance.expense_resource_link (expense_id, resource_type, resource_id, relation_type)
     VALUES ($1, 'MEDIA', $2, 'RELATED')
     ON CONFLICT (expense_id, resource_type, resource_id, relation_type) DO NOTHING`,
    [expenseId, stored.uploadId]
  );
  return stored.uploadId;
}

export async function createPersonalLifeExpense(
  client: PoolClient,
  ctx: RequestContext,
  body: CreatePersonalLifeExpenseInput
): Promise<PersonalExpenseConfirmation> {
  rejectNonEconomic(body.economicKind);
  assertContext(body.lifeAreaCode, body.lifeSubcategoryCode, body.moodCode);
  const amount = Number(body.amount);
  if (!(amount > 0)) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Amount must be greater than zero.', 400);
  }
  const momentId = await ensurePersonalLedger(client, ctx);
  const currencyCode = body.currencyCode.toUpperCase();
  const accountId = await resolveUserAccount(client, ctx, currencyCode, null);
  const accountType = await getAccountType(client, accountId);
  const paymentMethod = accountType ? derivePaymentMethodFromAccountType(accountType) : 'OTHER';
  const classification = body.classificationSource ?? 'manual';
  const inserted = await client.query<{ expense_id: string; version: string; effective_at: Date }>(
    `INSERT INTO finance.expense (
       moment_id, domain_code, financial_account_id, created_by_user_id, merchant_name, description,
       category_code, subcategory_code, payment_method_code, amount, currency_code, effective_at,
       status, posted_at, version, note,
       life_area_code, life_subcategory_code, mood_code, classification_source, origin, economic_kind, payment_source
     ) VALUES (
       $1, 'PERSONAL', $2, $3, $4, $5,
       $6, $7, $8, $9, $10, COALESCE($11::timestamptz, now()),
       'POSTED', now(), 1, $12,
       $6, $7, $13, $14, $15, 'EXPENSE', $16
     )
     RETURNING expense_id, version, effective_at`,
    [
      momentId,
      accountId,
      ctx.userId,
      body.merchantName ?? null,
      body.note ?? null,
      body.lifeAreaCode,
      body.lifeSubcategoryCode,
      paymentMethod,
      amount.toFixed(4),
      currencyCode,
      body.effectiveAt ?? null,
      body.note ?? null,
      body.moodCode ?? null,
      classification,
      body.origin ?? 'QUICK_ADD',
      body.paymentSource ?? null,
    ]
  );
  const expenseId = inserted.rows[0].expense_id;
  await client.query(
    `INSERT INTO finance.personal_expense_context (expense_id, moment_id, user_id) VALUES ($1, $2, $3)`,
    [expenseId, momentId, ctx.userId]
  );
  const photoUploadId = body.photo ? await linkExpensePhoto(client, ctx, expenseId, momentId, body.photo) : null;
  const { domainEventId } = await insertDomainEventAndOutbox(client, ctx, {
    eventName: 'personal_expense_created',
    domainCode: 'PERSONAL',
    aggregateType: 'EXPENSE',
    aggregateId: expenseId,
    scopeType: 'MOMENT',
    scopeId: momentId,
    payload: {
      expenseId,
      amount: amount.toFixed(4),
      currencyCode,
      lifeAreaCode: body.lifeAreaCode,
      lifeSubcategoryCode: body.lifeSubcategoryCode,
      moodCode: body.moodCode ?? null,
      origin: body.origin ?? 'QUICK_ADD',
    },
  });
  await insertAudit(client, ctx, 'EXPENSE_CREATE', 'EXPENSE', expenseId, domainEventId, {
    expenseId,
    lifeAreaCode: body.lifeAreaCode,
    lifeSubcategoryCode: body.lifeSubcategoryCode,
  });
  const experience = await getPersonalExperience(client, ctx.userId);
  return confirmationFrom(experience, {
    expenseId,
    momentId,
    amount: amount.toFixed(2),
    currencyCode,
    merchantName: body.merchantName ?? null,
    effectiveAt: inserted.rows[0].effective_at.toISOString(),
    lifeAreaCode: body.lifeAreaCode,
    lifeSubcategoryCode: body.lifeSubcategoryCode,
    moodCode: body.moodCode ?? null,
    version: parseInt(inserted.rows[0].version, 10),
    photoUploadId,
  });
}

async function loadOwnedLifeExpense(client: PoolClient, userId: string, expenseId: string) {
  const row = await client.query<{
    expense_id: string;
    moment_id: string;
    amount: string;
    currency_code: string;
    status: string;
    version: string;
    life_area_code: string | null;
    economic_kind: string;
  }>(
    `SELECT e.expense_id, e.moment_id, e.amount::text AS amount, e.currency_code, e.status, e.version::text AS version,
            e.life_area_code, e.economic_kind
     FROM finance.expense e
     JOIN finance.personal_expense_context pec ON pec.expense_id = e.expense_id
     WHERE e.expense_id = $1 AND pec.user_id = $2
     FOR UPDATE OF e`,
    [expenseId, userId]
  );
  if (!row.rows[0]) throw new AppError(ErrorCode.RESOURCE_NOT_FOUND, 'Expense not found.', 404);
  return row.rows[0];
}

export async function updatePersonalLifeExpense(
  client: PoolClient,
  ctx: RequestContext,
  expenseId: string,
  body: UpdatePersonalLifeExpenseInput
): Promise<PersonalExpenseConfirmation> {
  assertContext(body.lifeAreaCode, body.lifeSubcategoryCode, body.moodCode);
  const row = await loadOwnedLifeExpense(client, ctx.userId, expenseId);
  if (row.status !== 'POSTED' || row.economic_kind !== 'EXPENSE') {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Only a posted expense can be corrected.', 400);
  }
  const updated = await client.query<{
    version: string;
    effective_at: Date;
    merchant_name: string | null;
    amount: string;
    currency_code: string;
    mood_code: string | null;
  }>(
    `UPDATE finance.expense
     SET life_area_code = $2,
         life_subcategory_code = $3,
         category_code = $2,
         subcategory_code = $3,
         mood_code = CASE WHEN $7::bool THEN $4 ELSE mood_code END,
         merchant_name = COALESCE($5, merchant_name),
         note = COALESCE($6, note),
         classification_source = 'corrected',
         version = version + 1,
         updated_at = now()
     WHERE expense_id = $1
     RETURNING version::text AS version, effective_at, merchant_name, amount::text AS amount, currency_code, mood_code`,
    [
      expenseId,
      body.lifeAreaCode,
      body.lifeSubcategoryCode,
      body.moodCode ?? null,
      body.merchantName === undefined ? null : body.merchantName,
      body.note === undefined ? null : body.note,
      body.moodCode !== undefined,
    ]
  );
  await insertDomainEventAndOutbox(client, ctx, {
    eventName: 'personal_expense_context_changed',
    domainCode: 'PERSONAL',
    aggregateType: 'EXPENSE',
    aggregateId: expenseId,
    scopeType: 'MOMENT',
    scopeId: row.moment_id,
    payload: {
      expenseId,
      lifeAreaCode: body.lifeAreaCode,
      lifeSubcategoryCode: body.lifeSubcategoryCode,
      moodCode: body.moodCode ?? null,
    },
  });
  if (body.moodCode !== undefined) {
    await insertDomainEventAndOutbox(client, ctx, {
      eventName: 'personal_expense_mood_set',
      domainCode: 'PERSONAL',
      aggregateType: 'EXPENSE',
      aggregateId: expenseId,
      scopeType: 'MOMENT',
      scopeId: row.moment_id,
      payload: { expenseId, moodCode: body.moodCode },
    });
  }
  const saved = updated.rows[0];
  const photoUploadId = body.photo
    ? await linkExpensePhoto(client, ctx, expenseId, row.moment_id, body.photo)
    : null;
  const experience = await getPersonalExperience(client, ctx.userId);
  return confirmationFrom(experience, {
    expenseId,
    momentId: row.moment_id,
    amount: Number(saved.amount).toFixed(2),
    currencyCode: saved.currency_code,
    merchantName: saved.merchant_name,
    effectiveAt: saved.effective_at.toISOString(),
    lifeAreaCode: body.lifeAreaCode,
    lifeSubcategoryCode: body.lifeSubcategoryCode,
    moodCode: saved.mood_code,
    version: parseInt(saved.version, 10),
    photoUploadId,
  });
}

export async function refundPersonalLifeExpense(
  client: PoolClient,
  ctx: RequestContext,
  expenseId: string,
  amountText?: string
): Promise<{ expenseId: string; reversedExpenseId: string; amount: string }> {
  const row = await loadOwnedLifeExpense(client, ctx.userId, expenseId);
  if (row.status !== 'POSTED' || row.economic_kind !== 'EXPENSE' || !row.life_area_code) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Only a posted life expense can be refunded.', 400);
  }
  const original = Number(row.amount);
  const refundAmount = amountText != null ? Number(amountText) : original;
  if (!(refundAmount > 0) || refundAmount - original > 0.0001) {
    throw new AppError(ErrorCode.VALIDATION_FAILED, 'Refund amount must be greater than zero and not above the original.', 400);
  }
  const momentId = row.moment_id;
  const accountId = await resolveUserAccount(client, ctx, row.currency_code, null);
  const inserted = await client.query<{ expense_id: string }>(
    `INSERT INTO finance.expense (
       moment_id, domain_code, financial_account_id, created_by_user_id, merchant_name,
       category_code, subcategory_code, amount, currency_code, status, posted_at, version,
       life_area_code, life_subcategory_code, classification_source, origin, economic_kind, reversed_expense_id
     )
     SELECT moment_id, domain_code, $2, created_by_user_id, merchant_name,
            life_area_code, life_subcategory_code, $3, currency_code, 'POSTED', now(), 1,
            life_area_code, life_subcategory_code, 'corrected', 'REFUND', 'REFUND', expense_id
     FROM finance.expense WHERE expense_id = $1
     RETURNING expense_id`,
    [expenseId, accountId, refundAmount.toFixed(4)]
  );
  const refundId = inserted.rows[0].expense_id;
  await client.query(
    `INSERT INTO finance.personal_expense_context (expense_id, moment_id, user_id) VALUES ($1, $2, $3)`,
    [refundId, momentId, ctx.userId]
  );
  await insertDomainEventAndOutbox(client, ctx, {
    eventName: 'personal_expense_created',
    domainCode: 'PERSONAL',
    aggregateType: 'EXPENSE',
    aggregateId: refundId,
    scopeType: 'MOMENT',
    scopeId: momentId,
    payload: { expenseId: refundId, reversedExpenseId: expenseId, amount: refundAmount.toFixed(4), economicKind: 'REFUND' },
  });
  return { expenseId: refundId, reversedExpenseId: expenseId, amount: refundAmount.toFixed(2) };
}

export async function voidPersonalLifeExpense(
  client: PoolClient,
  ctx: RequestContext,
  expenseId: string
): Promise<{ expenseId: string; status: string }> {
  const row = await loadOwnedLifeExpense(client, ctx.userId, expenseId);
  if (row.status === 'VOIDED') return { expenseId, status: 'VOIDED' };
  await client.query(
    `UPDATE finance.expense SET status = 'VOIDED', version = version + 1, updated_at = now() WHERE expense_id = $1`,
    [expenseId]
  );
  await insertDomainEventAndOutbox(client, ctx, {
    eventName: 'personal_expense_voided',
    domainCode: 'PERSONAL',
    aggregateType: 'EXPENSE',
    aggregateId: expenseId,
    scopeType: 'MOMENT',
    scopeId: row.moment_id,
    payload: { expenseId, reason: 'VOIDED' },
  });
  return { expenseId, status: 'VOIDED' };
}
