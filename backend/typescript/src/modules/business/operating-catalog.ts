/**
 * Four operating Business Moments. Family config drives setup, Quick Add, and
 * which Pulse figures a role may see.
 */

export const OPERATING_FAMILY_CODES = [
  'MARKETING_CAMPAIGNS',
  'TRADING_DISTRIBUTION',
  'RETAIL_CONSUMER',
  'MANUFACTURING_PRODUCTION',
] as const;

export type OperatingFamilyCode = (typeof OPERATING_FAMILY_CODES)[number];

export const LEGACY_BUSINESS_FAMILY_CODES = [
  'TEAM_OPERATIONS',
  'BUSINESS_RUNWAY',
  'BUSINESS_OPERATIONS',
] as const;

export type OperatingRole =
  | 'OWNER_ADMIN'
  | 'OPERATIONS_LEAD'
  | 'FINANCE'
  | 'APPROVER'
  | 'CONTRIBUTOR'
  | 'TEAM_MEMBER'
  | 'OBSERVER';

export const OPERATING_ROLES: Array<{ code: OperatingRole; label: string }> = [
  { code: 'OWNER_ADMIN', label: 'Owner / Admin' },
  { code: 'OPERATIONS_LEAD', label: 'Operations Lead' },
  { code: 'FINANCE', label: 'Finance' },
  { code: 'APPROVER', label: 'Approver' },
  { code: 'CONTRIBUTOR', label: 'Contributor' },
  { code: 'TEAM_MEMBER', label: 'Team Member' },
  { code: 'OBSERVER', label: 'Observer' },
];

export type QuickAddCode =
  | 'SPEND'
  | 'RESULT'
  | 'PAYMENT'
  | 'COLLECTION'
  | 'BUDGET'
  | 'TARGET'
  | 'UPDATE'
  | 'ISSUE'
  | 'APPROVAL'
  | 'DELIVERABLE'
  | 'VENDOR_COST'
  | 'COMPLETE'
  | 'SALE'
  | 'PURCHASE'
  | 'STOCK'
  | 'ORDER'
  | 'SHIPMENT'
  | 'SERVICE'
  | 'APPOINTMENT'
  | 'PRODUCTION'
  | 'CUSTOMER_ORDER'
  | 'DELIVERY';

export type PaymentRelevance = 'NONE' | 'PAYABLE' | 'RECEIVABLE' | 'REIMBURSEMENT' | 'SETTLEMENT';

export interface QuickAddDef {
  code: QuickAddCode;
  label: string;
  consumesBudget: boolean;
  countsTarget: boolean;
  paymentRelevance: PaymentRelevance;
  financeOnly: boolean;
  needsAmount: boolean;
}

const QA = {
  SPEND: qa('SPEND', 'Add Spend', true, false, 'PAYABLE', false, true),
  RESULT: qa('RESULT', 'Add Result', false, true, 'NONE', false, true),
  PAYMENT: qa('PAYMENT', 'Add Payment', false, false, 'PAYABLE', true, true),
  COLLECTION: qa('COLLECTION', 'Add Collection', false, true, 'RECEIVABLE', true, true),
  BUDGET: qa('BUDGET', 'Revise Budget', false, false, 'NONE', true, true),
  TARGET: qa('TARGET', 'Revise Target', false, false, 'NONE', true, true),
  UPDATE: qa('UPDATE', 'Add Update', false, false, 'NONE', false, false),
  ISSUE: qa('ISSUE', 'Add Issue', false, false, 'NONE', false, false),
  APPROVAL: qa('APPROVAL', 'Request Approval', false, false, 'NONE', false, false),
  DELIVERABLE: qa('DELIVERABLE', 'Add Deliverable', false, false, 'NONE', false, false),
  VENDOR_COST: qa('VENDOR_COST', 'Add Vendor Cost', true, false, 'PAYABLE', false, true),
  COMPLETE: qa('COMPLETE', 'Mark Complete', false, false, 'NONE', false, false),
  SALE: qa('SALE', 'Record Sale', false, true, 'RECEIVABLE', false, true),
  PURCHASE: qa('PURCHASE', 'Record Purchase', true, false, 'PAYABLE', false, true),
  STOCK: qa('STOCK', 'Stock Update', false, false, 'NONE', false, false),
  ORDER: qa('ORDER', 'Order Update', false, false, 'NONE', false, false),
  SHIPMENT: qa('SHIPMENT', 'Shipment Update', false, false, 'NONE', false, false),
  SERVICE: qa('SERVICE', 'Record Service', false, true, 'RECEIVABLE', false, true),
  APPOINTMENT: qa('APPOINTMENT', 'Appointment / Job', false, false, 'NONE', false, false),
  PRODUCTION: qa('PRODUCTION', 'Record Production', false, true, 'NONE', false, true),
  CUSTOMER_ORDER: qa('CUSTOMER_ORDER', 'Customer Order', false, false, 'RECEIVABLE', false, true),
  DELIVERY: qa('DELIVERY', 'Record Delivery', false, false, 'NONE', false, false),
} satisfies Record<QuickAddCode, QuickAddDef>;

function qa(
  code: QuickAddCode,
  label: string,
  consumesBudget: boolean,
  countsTarget: boolean,
  paymentRelevance: PaymentRelevance,
  financeOnly: boolean,
  needsAmount: boolean
): QuickAddDef {
  return { code, label, consumesBudget, countsTarget, paymentRelevance, financeOnly, needsAmount };
}

export interface OperatingSubtype {
  code: string;
  label: string;
  partyLabel: string;
  quickAdds: QuickAddCode[];
  targetMetrics: string[];
  requiresChannel: boolean;
}

export interface OperatingFamily {
  familyCode: OperatingFamilyCode;
  title: string;
  subtitle: string;
  figmaNodeId: string;
  activateLabel: string;
  subtypes: OperatingSubtype[];
}

const MARKETING_ADDS: QuickAddCode[] = [
  'SPEND', 'RESULT', 'PAYMENT', 'BUDGET', 'TARGET', 'UPDATE', 'ISSUE', 'APPROVAL',
  'DELIVERABLE', 'VENDOR_COST', 'COMPLETE',
];

export const OPERATING_FAMILIES: OperatingFamily[] = [
  {
    familyCode: 'MARKETING_CAMPAIGNS',
    title: 'Marketing & Campaigns',
    subtitle: 'Run a client campaign or a campaign for your own business.',
    figmaNodeId: '2170:32721',
    activateLabel: 'Activate campaign',
    subtypes: [
      {
        code: 'CLIENT_CAMPAIGN',
        label: 'Client Campaign',
        partyLabel: 'Client',
        quickAdds: [...MARKETING_ADDS, 'COLLECTION'],
        targetMetrics: ['REVENUE', 'LEADS', 'ORDERS', 'CUSTOM_COUNT', 'CLIENT_BILLING'],
        requiresChannel: true,
      },
      {
        code: 'OWN_BUSINESS_CAMPAIGN',
        label: 'Own Business Campaign',
        partyLabel: 'Brand',
        quickAdds: [...MARKETING_ADDS, 'COLLECTION'],
        targetMetrics: ['REVENUE', 'LEADS', 'ORDERS', 'CUSTOM_COUNT'],
        requiresChannel: true,
      },
    ],
  },
  {
    familyCode: 'TRADING_DISTRIBUTION',
    title: 'Trading & Distribution',
    subtitle: 'Source, move, and collect across wholesale or distribution.',
    figmaNodeId: '2160:21022',
    activateLabel: 'Activate trading moment',
    subtypes: [
      {
        code: 'TRADING_WHOLESALE',
        label: 'Trading / Wholesale',
        partyLabel: 'Business',
        quickAdds: ['SALE', 'PURCHASE', 'SPEND', 'PAYMENT', 'COLLECTION', 'BUDGET', 'UPDATE', 'ISSUE', 'APPROVAL', 'STOCK', 'ORDER', 'SHIPMENT'],
        targetMetrics: ['REVENUE', 'ORDERS', 'CUSTOM_COUNT'],
        requiresChannel: false,
      },
      {
        code: 'DISTRIBUTION_DEALERSHIP',
        label: 'Distribution / Dealership',
        partyLabel: 'Principal',
        quickAdds: ['SALE', 'PURCHASE', 'SPEND', 'PAYMENT', 'COLLECTION', 'BUDGET', 'UPDATE', 'ISSUE', 'APPROVAL', 'STOCK', 'ORDER', 'SHIPMENT'],
        targetMetrics: ['REVENUE', 'ORDERS', 'CUSTOM_COUNT'],
        requiresChannel: false,
      },
    ],
  },
  {
    familyCode: 'RETAIL_CONSUMER',
    title: 'Retail & Consumer',
    subtitle: 'Sell through a store, a food outlet, or a consumer service.',
    figmaNodeId: '2166:32567',
    activateLabel: 'Activate retail moment',
    subtypes: [
      {
        code: 'RETAIL_STORE',
        label: 'Retail Store',
        partyLabel: 'Store',
        quickAdds: ['SALE', 'PURCHASE', 'SPEND', 'PAYMENT', 'COLLECTION', 'BUDGET', 'UPDATE', 'ISSUE', 'APPROVAL', 'STOCK', 'ORDER'],
        targetMetrics: ['REVENUE', 'ORDERS', 'CUSTOM_COUNT'],
        requiresChannel: false,
      },
      {
        code: 'RESTAURANT_FOOD',
        label: 'Restaurant / Food',
        partyLabel: 'Outlet',
        quickAdds: ['SALE', 'PURCHASE', 'SPEND', 'PAYMENT', 'COLLECTION', 'BUDGET', 'UPDATE', 'ISSUE', 'APPROVAL', 'STOCK', 'ORDER'],
        targetMetrics: ['REVENUE', 'ORDERS', 'CUSTOM_COUNT'],
        requiresChannel: false,
      },
      {
        code: 'CONSUMER_SERVICE',
        label: 'Consumer Service',
        partyLabel: 'Service',
        quickAdds: ['SERVICE', 'APPOINTMENT', 'SALE', 'SPEND', 'PAYMENT', 'COLLECTION', 'BUDGET', 'UPDATE', 'ISSUE', 'APPROVAL'],
        targetMetrics: ['REVENUE', 'ORDERS', 'CUSTOM_COUNT'],
        requiresChannel: false,
      },
    ],
  },
  {
    familyCode: 'MANUFACTURING_PRODUCTION',
    title: 'Manufacturing & Production',
    subtitle: 'Produce to stock or make to a customer order.',
    figmaNodeId: '2177:25239',
    activateLabel: 'Activate production moment',
    subtypes: [
      {
        code: 'STANDARD_PRODUCTION',
        label: 'Standard Production',
        partyLabel: 'Plant',
        quickAdds: ['PRODUCTION', 'PURCHASE', 'SPEND', 'SALE', 'CUSTOMER_ORDER', 'DELIVERY', 'PAYMENT', 'COLLECTION', 'BUDGET', 'TARGET', 'UPDATE', 'ISSUE', 'APPROVAL', 'STOCK'],
        targetMetrics: ['REVENUE', 'PRODUCTION_OUTPUT', 'UNITS_PRODUCED', 'VALUE_DISPATCHED'],
        requiresChannel: false,
      },
      {
        code: 'MADE_TO_ORDER',
        label: 'Made-to-Order',
        partyLabel: 'Workshop',
        quickAdds: ['PRODUCTION', 'PURCHASE', 'SPEND', 'SALE', 'CUSTOMER_ORDER', 'DELIVERY', 'PAYMENT', 'COLLECTION', 'BUDGET', 'TARGET', 'UPDATE', 'ISSUE', 'APPROVAL', 'STOCK'],
        targetMetrics: ['REVENUE', 'ORDERS', 'DELIVERY_COUNT', 'PRODUCTION_OUTPUT', 'CUSTOM_COUNT'],
        requiresChannel: false,
      },
    ],
  },
];

export function isOperatingFamily(code: string | null | undefined): code is OperatingFamilyCode {
  return OPERATING_FAMILY_CODES.includes(String(code ?? '').toUpperCase() as OperatingFamilyCode);
}

export function isLegacyBusinessFamily(code: string | null | undefined): boolean {
  const c = String(code ?? '').toUpperCase();
  return (LEGACY_BUSINESS_FAMILY_CODES as readonly string[]).includes(c);
}

export function isOperatingSubtype(code: string | null | undefined): boolean {
  const c = String(code ?? '').toUpperCase();
  return OPERATING_FAMILIES.some((f) => f.subtypes.some((s) => s.code === c));
}

export function familyForSubtype(subtype: string): OperatingFamily | undefined {
  const c = subtype.toUpperCase();
  return OPERATING_FAMILIES.find((f) => f.subtypes.some((s) => s.code === c));
}

export function subtypeDef(familyCode: string, subtype: string): OperatingSubtype | undefined {
  const family = OPERATING_FAMILIES.find((f) => f.familyCode === familyCode.toUpperCase());
  return family?.subtypes.find((s) => s.code === subtype.toUpperCase());
}

export function quickAddDef(code: string): QuickAddDef | undefined {
  return QA[code.toUpperCase() as QuickAddCode];
}

export function quickAddsForSubtype(subtype: string): QuickAddDef[] {
  const family = familyForSubtype(subtype);
  const sub = family?.subtypes.find((s) => s.code === subtype.toUpperCase());
  if (!sub) return [];
  return sub.quickAdds.map((code) => QA[code]);
}

const FINANCE_VISIBLE = new Set<OperatingRole>(['OWNER_ADMIN', 'FINANCE', 'APPROVER']);
const APPROVER_ROLES = new Set<OperatingRole>(['OWNER_ADMIN', 'FINANCE', 'APPROVER']);
const AUTO_POST = new Set<OperatingRole>(['OWNER_ADMIN', 'FINANCE', 'OPERATIONS_LEAD']);

export function financeVisibleFor(role: OperatingRole): boolean {
  return FINANCE_VISIBLE.has(role);
}

export function canApprove(role: OperatingRole): boolean {
  return APPROVER_ROLES.has(role);
}

export function canSubmit(role: OperatingRole, def: QuickAddDef): boolean {
  if (role === 'OBSERVER') return false;
  if (def.financeOnly && !FINANCE_VISIBLE.has(role) && role !== 'OPERATIONS_LEAD') return false;
  if (role === 'TEAM_MEMBER' && (def.consumesBudget || def.financeOnly || def.paymentRelevance !== 'NONE')) {
    return false;
  }
  return true;
}

/** Controlled money actions from a non-posting role wait for a different approver. */
export function needsMakerChecker(role: OperatingRole, def: QuickAddDef): boolean {
  if (def.code === 'APPROVAL' || def.code === 'BUDGET' || def.code === 'TARGET' || def.code === 'PAYMENT' || def.code === 'COLLECTION') {
    return !AUTO_POST.has(role);
  }
  if (def.consumesBudget || def.paymentRelevance === 'RECEIVABLE') {
    return role === 'CONTRIBUTOR' || role === 'TEAM_MEMBER';
  }
  return false;
}

export interface OperatingParticipant {
  displayName: string;
  role: OperatingRole;
  userId?: string;
}

export interface OperatingCounterparty {
  name: string;
  kind: string;
}

export type ActivationPolicy = 'NONE' | 'APPROVAL_REQUIRED';

export interface OperatingPreferences {
  subtype: string;
  identityName: string;
  partyName: string;
  description: string;
  currency: string;
  startDate: string;
  location: string;
  activationPolicy: ActivationPolicy;
  participants: OperatingParticipant[];
  counterparties: OperatingCounterparty[];
  channels: string[];
  tradeReach: string[];
  monthlyBudget: string;
  monthlyTarget: string;
  targetMetric: string;
}

function asRole(value: unknown): OperatingRole | undefined {
  const code = String(value ?? '').toUpperCase() as OperatingRole;
  return OPERATING_ROLES.some((r) => r.code === code) ? code : undefined;
}

function positiveAmount(value: unknown): string | undefined {
  if (typeof value === 'number' && Number.isFinite(value) && value > 0) return value.toFixed(4);
  const raw = String(value ?? '').replace(/[₹,\s]/g, '');
  if (!/^\d+(\.\d{1,4})?$/.test(raw)) return undefined;
  const n = Number(raw);
  if (!Number.isFinite(n) || n <= 0) return undefined;
  return n.toFixed(4);
}

export function normalizeOperatingPreferences(
  familyCode: OperatingFamilyCode,
  input: Record<string, unknown> | undefined,
  creator: { userId: string; displayName: string }
): OperatingPreferences {
  const prefs = input ?? {};
  const subtype = String(prefs.subtype ?? '').toUpperCase();
  const sub = subtypeDef(familyCode, subtype);
  if (!sub) {
    throw new Error(`Unknown subtype for ${familyCode}: ${subtype || '(missing)'}`);
  }
  const identityName = String(prefs.identityName ?? prefs.title ?? '').trim();
  const partyName = String(prefs.partyName ?? '').trim();
  if (!identityName) throw new Error('Moment name is required.');
  if (!partyName) throw new Error(`${sub.partyLabel} name is required.`);
  const monthlyBudget = positiveAmount(prefs.monthlyBudget);
  const monthlyTarget = positiveAmount(prefs.monthlyTarget);
  if (!monthlyBudget) throw new Error('Monthly budget must be a positive amount.');
  if (!monthlyTarget) throw new Error('Monthly target must be a positive amount.');
  const targetMetric = String(prefs.targetMetric ?? sub.targetMetrics[0]).toUpperCase();
  if (!sub.targetMetrics.includes(targetMetric)) {
    throw new Error(`Target measure must be one of ${sub.targetMetrics.join(', ')}.`);
  }
  const channels = Array.isArray(prefs.channels)
    ? prefs.channels.map((c) => String(c).trim()).filter(Boolean)
    : [];
  const reachAllowed = new Set(['Local', 'Domestic', 'International']);
  const tradeReach = Array.isArray(prefs.tradeReach)
    ? [...new Set(prefs.tradeReach.map((item) => String(item).trim()).filter((item) => reachAllowed.has(item)))]
    : [];
  if (sub.requiresChannel && channels.length === 0) {
    throw new Error('Select at least one channel.');
  }
  const participants: OperatingParticipant[] = [];
  if (Array.isArray(prefs.participants)) {
    for (const row of prefs.participants) {
      if (!row || typeof row !== 'object') continue;
      const rec = row as Record<string, unknown>;
      const role = asRole(rec.role);
      const displayName = String(rec.displayName ?? '').trim();
      if (!role || !displayName) continue;
      participants.push({
        displayName,
        role,
        userId: typeof rec.userId === 'string' ? rec.userId : undefined,
      });
    }
  }
  if (!participants.some((p) => p.userId === creator.userId)) {
    participants.unshift({
      displayName: creator.displayName || 'Owner',
      role: 'OWNER_ADMIN',
      userId: creator.userId,
    });
  }
  const counterparties: OperatingCounterparty[] = [];
  if (Array.isArray(prefs.counterparties)) {
    for (const row of prefs.counterparties) {
      if (!row || typeof row !== 'object') continue;
      const rec = row as Record<string, unknown>;
      const name = String(rec.name ?? '').trim();
      if (!name) continue;
      counterparties.push({ name, kind: String(rec.kind ?? 'OTHER').trim() || 'OTHER' });
    }
  }
  return {
    subtype,
    identityName,
    partyName,
    description: String(prefs.description ?? '').trim(),
    currency: String(prefs.currency ?? 'INR').toUpperCase(),
    startDate: String(prefs.startDate ?? ''),
    location: String(prefs.location ?? '').trim(),
    activationPolicy: String(prefs.activationPolicy ?? '').toUpperCase() === 'APPROVAL_REQUIRED'
      ? 'APPROVAL_REQUIRED'
      : 'NONE',
    participants,
    counterparties,
    channels,
    tradeReach,
    monthlyBudget,
    monthlyTarget,
    targetMetric,
  };
}

export interface OperatingEventRow {
  operatingEventId: string;
  eventType: string;
  title: string;
  note: string | null;
  amount: string | null;
  currencyCode: string | null;
  counterpartyName: string | null;
  channelName: string | null;
  paymentRelevance: string;
  paymentStatus: string;
  approvalStatus: string;
  submittedByUserId: string;
  decidedByUserId: string | null;
  occurredAt: string;
}

export interface OperatingProjection {
  retired: boolean;
  familyCode: string;
  subtype: string;
  subtypeLabel: string;
  title: string;
  partyName: string;
  role: OperatingRole;
  financeVisible: boolean;
  currency: string;
  startDate: string;
  location: string;
  budget: { amount: string; spent: string; remaining: string } | null;
  target: { metric: string; value: string; progress: string } | null;
  finance: OperatingFinanceTotals | null;
  attention: Array<{ id: string; title: string; kind: string }>;
  paymentReadiness: Array<{ id: string; title: string; amount: string; relevance: string; status: string }>;
  activity: Array<{
    id: string;
    title: string;
    eventType: string;
    occurredAt: string;
    amount: string | null;
    channelName: string | null;
    counterpartyName: string | null;
    paymentStatus: string | null;
    approvalStatus: string | null;
    note: string | null;
  }>;
  life: { channels: string[]; openCommitments: number; resultProgress: string };
  memory: { revisions: Array<{ id: string; title: string; occurredAt: string }> };
  boards: {
    moments: OperatingBoardSection[];
    life: OperatingBoardSection[];
    memory: OperatingBoardSection[];
  };
  quickAdds: Array<{ code: string; label: string; needsAmount: boolean }>;
}

export interface OperatingBoardRow {
  id: string;
  title: string;
  eventType: string;
  occurredAt: string;
  amount: string | null;
  channelName: string | null;
  counterpartyName: string | null;
  paymentStatus: string | null;
  approvalStatus: string | null;
  note: string | null;
}

export interface OperatingFinanceTotals {
  sales: string;
  received: string;
  outstanding: string;
  eligibleCost: string;
  cashPaid: string;
}

export interface OperatingBoardSection {
  code: string;
  title: string;
  detail: string;
  rows: OperatingBoardRow[];
}

function num(value: string | null | undefined): number {
  const n = Number(value ?? 0);
  return Number.isFinite(n) ? n : 0;
}

function manufacturingTargetAmount(metric: string, eventType: string, amount: number): number {
  switch (metric) {
    case 'REVENUE':
      return eventType === 'SALE' ? amount : 0;
    case 'PRODUCTION_OUTPUT':
    case 'UNITS_PRODUCED':
    case 'CUSTOM_COUNT':
      return eventType === 'PRODUCTION' ? amount : 0;
    case 'VALUE_DISPATCHED':
      return eventType === 'DELIVERY' ? amount : 0;
    case 'ORDERS':
      return eventType === 'CUSTOMER_ORDER' ? 1 : 0;
    case 'DELIVERY_COUNT':
      return eventType === 'DELIVERY' ? 1 : 0;
    default:
      return 0;
  }
}

function money(value: number): string {
  return value.toFixed(4);
}

function effective(row: OperatingEventRow): boolean {
  return row.approvalStatus !== 'PENDING' && row.approvalStatus !== 'REJECTED' && row.paymentStatus !== 'CANCELLED';
}

export function projectOperatingMoment(args: {
  familyCode: string;
  prefs: OperatingPreferences;
  role: OperatingRole;
  events: OperatingEventRow[];
}): OperatingProjection {
  const sub = subtypeDef(args.familyCode, args.prefs.subtype);
  const visible = financeVisibleFor(args.role);
  let budget = num(args.prefs.monthlyBudget);
  let target = num(args.prefs.monthlyTarget);
  let spent = 0;
  let progress = 0;
  const revisions: OperatingProjection['memory']['revisions'] = [];
  for (const row of args.events) {
    if (!effective(row)) continue;
    const def = quickAddDef(row.eventType);
    const amount = num(row.amount);
    if (row.eventType === 'BUDGET' && amount > 0) {
      budget = amount;
      revisions.push({ id: row.operatingEventId, title: row.title, occurredAt: row.occurredAt });
    } else if (row.eventType === 'TARGET' && amount > 0) {
      target = amount;
      revisions.push({ id: row.operatingEventId, title: row.title, occurredAt: row.occurredAt });
    }
    if (def?.consumesBudget) spent += amount;
    if (args.familyCode === 'MARKETING_CAMPAIGNS') {
      if (row.eventType === 'RESULT') progress += amount;
    } else if (args.familyCode === 'MANUFACTURING_PRODUCTION') {
      progress += manufacturingTargetAmount(args.prefs.targetMetric, row.eventType, amount);
    } else if (def?.countsTarget) {
      progress += amount;
    }
  }
  const attention = args.events
    .filter((row) => row.eventType === 'ISSUE' || row.approvalStatus === 'PENDING')
    .filter((row) => row.approvalStatus !== 'REJECTED')
    .map((row) => ({
      id: row.operatingEventId,
      title: row.title,
      kind: row.approvalStatus === 'PENDING' ? 'APPROVAL' : 'ISSUE',
    }));
  const paymentReadiness = visible
    ? args.events
        .filter(
          (row) =>
            row.paymentRelevance !== 'NONE' &&
            (row.paymentStatus === 'PENDING' || row.paymentStatus === 'READY') &&
            row.approvalStatus !== 'REJECTED' &&
            row.approvalStatus !== 'PENDING'
        )
        .map((row) => ({
          id: row.operatingEventId,
          title: row.title,
          amount: row.amount ?? '0.0000',
          relevance: row.paymentRelevance,
          status: row.paymentStatus,
        }))
    : [];
  const quickAdds = (sub?.quickAdds ?? [])
    .map((code) => QA[code])
    .filter((def) => canSubmit(args.role, def))
    .map((def) => ({ code: def.code, label: def.label, needsAmount: def.needsAmount }));
  return {
    retired: false,
    familyCode: args.familyCode,
    subtype: args.prefs.subtype,
    subtypeLabel: sub?.label ?? args.prefs.subtype,
    title: args.prefs.identityName,
    partyName: args.prefs.partyName,
    role: args.role,
    financeVisible: visible,
    currency: args.prefs.currency,
    startDate: args.prefs.startDate,
    location: args.prefs.location,
    finance: financeTotals(args.events, visible),
    budget: visible
      ? { amount: money(budget), spent: money(spent), remaining: money(budget - spent) }
      : null,
    target: visible
      ? { metric: args.prefs.targetMetric, value: money(target), progress: money(progress) }
      : null,
    attention,
    paymentReadiness,
    activity: args.events.map((row) => ({
      id: row.operatingEventId,
      title: row.title,
      eventType: row.eventType,
      occurredAt: row.occurredAt,
      amount: visible ? row.amount : null,
      channelName: row.channelName,
      counterpartyName: row.counterpartyName,
      paymentStatus: row.paymentStatus,
      approvalStatus: row.approvalStatus,
      note: row.note,
    })),
    life: {
      channels: args.prefs.channels,
      openCommitments: args.events.filter(
        (row) =>
          (row.eventType === 'ORDER' || row.eventType === 'CUSTOMER_ORDER' || row.eventType === 'DELIVERABLE') &&
          row.approvalStatus !== 'REJECTED'
      ).length,
      resultProgress: visible ? money(progress) : '',
    },
    memory: { revisions },
    boards: operatingBoards({
      familyCode: args.familyCode,
      subtype: args.prefs.subtype,
      prefs: args.prefs,
      events: args.events,
      visible,
      revisions,
    }),
    quickAdds,
  };
}

function emptyFacts(): Pick<OperatingBoardRow, 'channelName' | 'counterpartyName' | 'paymentStatus' | 'approvalStatus' | 'note'> {
  return {
    channelName: null,
    counterpartyName: null,
    paymentStatus: null,
    approvalStatus: null,
    note: null,
  };
}

function financeTotals(events: OperatingEventRow[], visible: boolean): OperatingFinanceTotals | null {
  if (!visible) return null;
  const rows = events.filter(effective);
  const sum = (match: (row: OperatingEventRow) => boolean) =>
    money(rows.filter(match).reduce((total, row) => total + num(row.amount), 0));
  return {
    sales: sum((row) => row.eventType === 'SALE' || row.eventType === 'SERVICE'),
    received: sum(
      (row) =>
        (row.eventType === 'COLLECTION' || row.eventType === 'SALE' || row.eventType === 'SERVICE') &&
        row.paymentStatus === 'COLLECTED'
    ),
    outstanding: sum(
      (row) =>
        row.paymentRelevance === 'RECEIVABLE' &&
        row.paymentStatus !== 'COLLECTED' &&
        row.paymentStatus !== 'PAID' &&
        row.paymentStatus !== 'CANCELLED'
    ),
    eligibleCost: sum((row) => quickAddDef(row.eventType)?.consumesBudget === true),
    cashPaid: sum((row) => row.eventType === 'PAYMENT' && row.paymentStatus === 'PAID'),
  };
}

function boardRow(row: OperatingEventRow, visible: boolean): OperatingBoardRow {
  return {
    id: row.operatingEventId,
    title: row.title,
    eventType: row.eventType,
    occurredAt: row.occurredAt,
    amount: visible ? row.amount : null,
    channelName: row.channelName,
    counterpartyName: row.counterpartyName,
    paymentStatus: row.paymentStatus,
    approvalStatus: row.approvalStatus,
    note: row.note,
  };
}

function eventSection(
  code: string,
  title: string,
  events: OperatingEventRow[],
  types: string[],
  visible: boolean,
  currency: string
): OperatingBoardSection {
  const matched = events.filter((row) => types.includes(row.eventType) && row.approvalStatus !== 'REJECTED');
  const rows = matched.map((row) => boardRow(row, visible));
  let detail = 'Nothing recorded yet.';
  if (rows.length > 0) {
    detail = `${rows.length} recorded`;
    if (visible) {
      const total = matched.reduce((sum, row) => sum + num(row.amount), 0);
      if (total > 0) detail = `${detail} · ${money(total)} ${currency}`;
    }
  }
  return { code, title, detail, rows };
}

function textSection(code: string, title: string, lines: string[]): OperatingBoardSection {
  const rows = lines.filter((line) => line.trim().length > 0).map((line, index) => ({
    id: `${code}-${index}`,
    title: line,
    eventType: code,
    occurredAt: '',
    amount: null,
    ...emptyFacts(),
  }));
  return {
    code,
    title,
    detail: rows.length > 0 ? `${rows.length} recorded` : 'Nothing recorded yet.',
    rows,
  };
}

const MONEY_MOVING_TYPES = ['SALE', 'PAYMENT', 'COLLECTION', 'SPEND', 'VENDOR_COST'];
const MONEY_HISTORY_TYPES = ['BUDGET', 'TARGET', 'SPEND', 'PAYMENT', 'COLLECTION', 'VENDOR_COST'];

type WidgetLabels = {
  m1: string;
  m3: string;
  l1: string;
  l2: string;
  l4: string;
  me3: string;
};

function widgetLabels(familyCode: string): WidgetLabels {
  if (familyCode === 'MARKETING_CAMPAIGNS') {
    return {
      m1: 'Campaign Moment',
      m3: 'Network & Channels',
      l1: 'Current Campaign',
      l2: 'Channels',
      l4: 'Execution',
      me3: 'People & Activity History',
    };
  }
  if (familyCode === 'TRADING_DISTRIBUTION') {
    return {
      m1: 'Business Moment',
      m3: 'Trade Network',
      l1: 'Current Business',
      l2: 'Channel Activity',
      l4: 'Operations',
      me3: 'People & Activity',
    };
  }
  if (familyCode === 'MANUFACTURING_PRODUCTION') {
    return {
      m1: 'My Manufacturing Business',
      m3: 'Business Network',
      l1: 'Current Production / Business',
      l2: 'Orders / Buyers',
      l4: 'Production Operations',
      me3: 'People & Activity',
    };
  }
  return {
    m1: 'Business Moment',
    m3: 'Business Network',
    l1: 'Current Business',
    l2: 'Channel Activity',
    l4: 'Operations',
    me3: 'People & Activity',
  };
}

function commitmentTypes(familyCode: string): string[] {
  if (familyCode === 'MARKETING_CAMPAIGNS') return ['DELIVERABLE', 'COMPLETE'];
  if (familyCode === 'MANUFACTURING_PRODUCTION') return ['ORDER', 'CUSTOMER_ORDER', 'SHIPMENT'];
  if (familyCode === 'TRADING_DISTRIBUTION') return ['ORDER', 'SHIPMENT'];
  return ['ORDER'];
}

function executionTypes(familyCode: string): string[] {
  if (familyCode === 'MARKETING_CAMPAIGNS') return ['RESULT', 'DELIVERABLE', 'UPDATE', 'ISSUE'];
  if (familyCode === 'MANUFACTURING_PRODUCTION') return ['ISSUE', 'STOCK', 'UPDATE', 'PRODUCTION', 'DELIVERY'];
  if (familyCode === 'RETAIL_CONSUMER') return ['ISSUE', 'STOCK', 'UPDATE', 'SERVICE', 'APPOINTMENT'];
  return ['ISSUE', 'STOCK', 'UPDATE'];
}

function operatingBoards(args: {
  familyCode: string;
  subtype: string;
  prefs: OperatingPreferences;
  events: OperatingEventRow[];
  visible: boolean;
  revisions: Array<{ id: string; title: string; occurredAt: string }>;
}): OperatingProjection['boards'] {
  const { familyCode, prefs, events, visible } = args;
  const labels = widgetLabels(familyCode);
  const currency = prefs.currency;
  const section = (code: string, title: string, types: string[]) =>
    eventSection(code, title, events, types, visible, currency);
  const names = prefs.participants.map((person) => person.displayName);
  const network = [...prefs.counterparties.map((party) => party.name), ...prefs.channels];
  const moments = [
    textSection('M1', labels.m1, [prefs.identityName, prefs.partyName, prefs.description]),
    textSection('M2', 'People', names),
    textSection('M3', labels.m3, network),
    section('M4', 'Open Commitments', commitmentTypes(familyCode)),
  ];
  const life = [
    textSection('L1', labels.l1, [prefs.identityName, prefs.partyName, args.subtype]),
    familyCode === 'MANUFACTURING_PRODUCTION'
      ? section('L2', labels.l2, ['CUSTOMER_ORDER', 'ORDER'])
      : textSection('L2', labels.l2, prefs.channels),
    section('L3', 'Money Moving', MONEY_MOVING_TYPES),
    section('L4', labels.l4, executionTypes(familyCode)),
    textSection('L5', 'People Today', names),
  ];
  const memory = [
    {
      code: 'ME1',
      title: 'Monthly History',
      detail: args.revisions.length > 0 ? `${args.revisions.length} recorded` : 'Nothing recorded yet.',
      rows: args.revisions.map((row) => ({
        id: row.id,
        title: row.title,
        eventType: 'REVISION',
        occurredAt: row.occurredAt,
        amount: null,
        ...emptyFacts(),
      })),
    },
    section('ME2', 'Money History', MONEY_HISTORY_TYPES),
    section('ME3', labels.me3, ['UPDATE']),
    section('ME4', 'Patterns & Decisions', ['ISSUE', 'APPROVAL']),
  ];
  return { moments, life, memory };
}

export function resolveRole(
  participants: OperatingParticipant[],
  userId: string,
  membershipType: string
): OperatingRole {
  const match = participants.find((p) => p.userId === userId);
  if (match) return match.role;
  if (membershipType === 'OWNER' || membershipType === 'ADMIN') return 'OWNER_ADMIN';
  return 'TEAM_MEMBER';
}
