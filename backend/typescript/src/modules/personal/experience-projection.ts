/**
 * Personal experience projections. Widgets read these facts; they are not a second ledger.
 * Thresholds are calibration defaults (spec D-06), not UI copy.
 */
import {
  PERSONAL_LIFE_AREAS,
  PERSONAL_LIFE_SUBCATEGORIES,
  PERSONAL_MOODS,
  getPersonalLifeArea,
  getPersonalLifeSubcategory,
  personalLifeLayerOf,
  type PersonalLifeLayer,
  type PersonalMoodCode,
} from './taxonomy.generated';

/** Groceries stay category activity unless a shared note names a higher-order story. */
export const TRIVIAL_SUBCATEGORIES = ['P1.2'] as const;

export const MOMENT_DETECTION = {
  windowDays: 30,
  emergingMinCount: 2,
  activeMinCount: 3,
  activeMinSpanDays: 7,
  completedQuietDays: 45,
  lookBackDays: 365,
  lookBackWindowDays: 45,
  moodSampleMin: 1,
  trendMaterialRatio: 0.25,
  maxActive: 4,
  maxEmerging: 2,
} as const;

export const NON_ECONOMIC_KINDS = ['TRANSFER', 'SETTLEMENT', 'WALLET_LOAD', 'CASH_WITHDRAWAL'] as const;

export interface LifeExpenseFact {
  expenseId: string;
  amount: number;
  currencyCode: string;
  merchantName: string | null;
  note: string | null;
  areaCode: string | null;
  subcategoryCode: string | null;
  moodCode: string | null;
  classificationSource: string | null;
  effectiveAt: string;
  status: string;
  origin: string | null;
  economicKind: 'EXPENSE' | 'REFUND';
  reversedExpenseId: string | null;
}

export interface PersonalExperience {
  currencyCode: string | null;
  expenseCount: number;
  cold: boolean;
  pulse: PulseProjection;
  moments: MomentsProjection;
  life: LifeProjection;
  memory: MemoryProjection;
}

interface PulseProjection {
  recent: Array<{
    expenseId: string;
    merchantName: string;
    amount: string;
    currencyCode: string;
    effectiveAt: string;
    moodCode: string | null;
    moodLabel: string | null;
    areaCode: string;
    areaLabel: string;
    subcategoryCode: string;
    subcategoryLabel: string;
    iconKey: string;
  }>;
  intro: { kicker: string; title: string; subtitle: string };
  attention: { quiet: boolean; issueCount: number; message: string; title: string; body: string };
  snapshot: {
    periodLabel: string;
    rangeLabel: string;
    total: string;
    expenseCount: number;
    previousTotal: string | null;
    previousLabel: string | null;
    previousRangeLabel: string | null;
    previousExpenseCount: number;
    comparable: boolean;
    deltaAmount: string | null;
    deltaRatio: number | null;
    comparisonNote: string;
    topAreaLabel: string | null;
    topAreaAmount: string | null;
    currentShort: string;
    previousShort: string | null;
  };
  signals: Array<{
    areaLabel: string;
    subcategoryLabel: string;
    current: string;
    previous: string;
    currentCount: number;
    previousCount: number;
    direction: 'up' | 'down';
    sentence: string;
  }>;
  signalsPrompt: string | null;
  signalsMessage: string | null;
  mood: {
    responseCount: number;
    expenseCount: number;
    coverage: number | null;
    insufficient: boolean;
    message: string;
    counts: Array<{ moodCode: string; label: string; count: number }>;
  };
}

interface MomentCard {
  id: string;
  state: 'emerging' | 'active' | 'completed';
  title: string;
  statusLabel: string;
  summary: string;
  rangeLabel: string;
  areaCode: string;
  areaLabel: string;
  subcategoryCode: string;
  subcategoryLabel: string;
  spend: string;
  activityCount: number;
  lastActivityAt: string;
  artworkKey: string | null;
  tentative: boolean;
}

interface MomentsProjection {
  intro: { kicker: string; title: string; subtitle: string };
  heroMonth: string | null;
  heroEvidence: string | null;
  activeSubtitle: string | null;
  emergingSubtitle: string | null;
  progressSubtitle: string | null;
  recentSubtitle: string | null;
  active: MomentCard[];
  emerging: MomentCard[];
  progress: {
    title: string;
    areaLabel: string;
    subcategoryLabel: string;
    bars: Array<{ effectiveAt: string; amount: string; label: string }>;
    cumulativeLabel: string;
    footnote: string;
    message: string;
  } | null;
  recent: MomentCard[];
  insight: {
    title: string | null;
    text: string;
    sufficient: boolean;
    actionLabel: string | null;
    footnote: string;
  };
}

interface LifeProjection {
  intro: { kicker: string; title: string; subtitle: string };
  total: string;
  expenseCount: number;
  monthLabel: string;
  rangeLabel: string;
  coverageLabel: string;
  highlightedAreaCode: string | null;
  areas: Array<{
    areaCode: string;
    label: string;
    layer: PersonalLifeLayer;
    iconKey: string;
    spend: string;
    activityCount: number;
    share: number;
  }>;
  nowBuild: { now: string; build: string; nowShare: number | null; buildShare: number | null };
  moneyGoes: Array<{ subcategoryCode: string; label: string; areaLabel: string; spend: string; share: number }>;
  otherCount: number;
  otherSpend: string;
  otherShare: number;
  topFiveSpend: string;
  topFiveShare: number;
  trends: Array<{
    label: string;
    areaLabel: string;
    direction: 'up' | 'down' | 'stable';
    detail: string;
    previous: string;
    current: string;
    previousCount: number;
    currentCount: number;
    previousShort: string;
    currentShort: string;
  }>;
  trendsMessage: string | null;
  trendsFootnote: string | null;
  moodSummary: string;
  moodCoverageLabel: string;
  moodFootnote: string;
  moodByArea: Array<{
    areaLabel: string;
    moodLabel: string | null;
    responseCount: number;
    expenseCount: number;
    noMoodRecorded: boolean;
    responses: string[];
  }>;
}

interface MemoryProjection {
  intro: { kicker: string; title: string; subtitle: string };
  openMonthShort: string;
  openMonthYear: number;
  openMonthChip: string;
  openRecap: string;
  openMonthNote: string;
  timelineOpenKicker: string;
  currentFilterLabel: string;
  timeline: Array<{
    expenseId: string;
    title: string;
    effectiveAt: string;
    detail: string;
    kicker: string;
    period: 'current' | 'closed';
  }>;
  completed: MomentCard[];
  monthly: {
    label: string;
    shortLabel: string;
    year: number;
    closedLabel: string;
    title: string;
    spend: string;
    expenseCount: number;
    completedCount: number;
    moodCount: number;
    moodCoverage: number;
    periodCaption: string;
    summary: string;
    openLabel: string;
  } | null;
  milestones: Array<{
    title: string;
    effectiveAt: string;
    detail: string;
    areaLabel: string;
    subcategoryLabel: string;
    spend: string;
    activityCount: number;
    confirmedLabel: string;
  }>;
  lookBack: { title: string; detail: string; effectiveAt: string; kicker: string } | null;
  lookBackMessage: string | null;
}

function money(n: number): string {
  return n.toFixed(2);
}

function moodLabel(code: string | null): string | null {
  if (!code) return null;
  return PERSONAL_MOODS.find((m) => m.code === code)?.label ?? null;
}

function areaLabel(code: string): string {
  return getPersonalLifeArea(code)?.label ?? code;
}

function subLabel(code: string): string {
  return getPersonalLifeSubcategory(code)?.label ?? code;
}

function iconKey(subCode: string): string {
  return getPersonalLifeSubcategory(subCode)?.iconKey ?? 'receipt';
}

function artworkFor(subCode: string): string | null {
  if (subCode.startsWith('P6')) return 'art_pers_design_course';
  if (subCode === 'P4.1') return 'art_pers_goa_sunset';
  if (subCode === 'P4.4' || subCode === 'P5.6') return 'art_pers_pottery_keepsake';
  return 'art_pers_open_beginning';
}

function monthKey(iso: string): string {
  const d = new Date(iso);
  return `${d.getUTCFullYear()}-${String(d.getUTCMonth() + 1).padStart(2, '0')}`;
}

function monthLabel(key: string): string {
  const [y, m] = key.split('-').map((x) => parseInt(x, 10));
  return new Date(Date.UTC(y, m - 1, 1)).toLocaleString('en-US', { month: 'long', year: 'numeric', timeZone: 'UTC' });
}

function addMonths(key: string, delta: number): string {
  const [y, m] = key.split('-').map((x) => parseInt(x, 10));
  const d = new Date(Date.UTC(y, m - 1 + delta, 1));
  return `${d.getUTCFullYear()}-${String(d.getUTCMonth() + 1).padStart(2, '0')}`;
}

function monthParts(key: string): { year: number; monthIndex: number } {
  const [y, m] = key.split('-').map((x) => parseInt(x, 10));
  return { year: y, monthIndex: m - 1 };
}

function shortMonth(key: string): string {
  const { year, monthIndex } = monthParts(key);
  return new Date(Date.UTC(year, monthIndex, 1)).toLocaleString('en-US', { month: 'short', timeZone: 'UTC' });
}

function daySpan(day: number): string {
  const end = String(day).padStart(2, '0');
  return `01–${end}`;
}

interface NetExpense extends LifeExpenseFact {
  net: number;
}

function hasLifeContext(row: NetExpense): row is ClassifiedExpense {
  return Boolean(row.areaCode && row.subcategoryCode);
}

function attentionState(classifiedCount: number, inferred: number, missingContext: number) {
  const issueCount = inferred + missingContext;
  if (issueCount === 0) {
    return {
      quiet: true,
      issueCount: 0,
      message: 'Nothing waiting for review.',
      title: `All ${classifiedCount} classified. You're clear.`,
      body: 'Every expense has a Life Area and subcategory. No issues to review.',
    };
  }
  if (missingContext === 0) {
    return {
      quiet: false,
      issueCount,
      message: `${inferred} expenses need a life context.`,
      title: `${inferred} need a life context.`,
      body: `${inferred} expenses need a Life Area and subcategory.`,
    };
  }
  const review = inferred > 0 ? ` ${inferred} more need review.` : '';
  return {
    quiet: false,
    issueCount,
    message: `${missingContext} expenses need a Life Area.${review}`,
    title: `${missingContext} need a Life Area.`,
    body: `${missingContext} expenses have no Life Area or subcategory.${review}`,
  };
}

function economicExpenses(facts: LifeExpenseFact[], currency: string | null): NetExpense[] {
  const posted = facts.filter((f) => f.status === 'POSTED' && (currency == null || f.currencyCode === currency));
  const refunds = posted.filter((f) => f.economicKind === 'REFUND' && f.reversedExpenseId);
  return posted
    .filter((f) => f.economicKind === 'EXPENSE')
    .map((f) => {
      const refunded = refunds
        .filter((r) => r.reversedExpenseId === f.expenseId)
        .reduce((s, r) => s + r.amount, 0);
      return { ...f, net: Math.max(0, f.amount - refunded) };
    });
}

function pickCurrency(facts: LifeExpenseFact[]): string | null {
  const counts = new Map<string, number>();
  for (const f of facts) {
    if (f.status !== 'POSTED' || f.economicKind !== 'EXPENSE') continue;
    counts.set(f.currencyCode, (counts.get(f.currencyCode) ?? 0) + 1);
  }
  let best: string | null = null;
  let n = 0;
  for (const [code, count] of counts) {
    if (count > n) {
      best = code;
      n = count;
    }
  }
  return best;
}

type ClassifiedExpense = NetExpense & { areaCode: string; subcategoryCode: string };

interface Cluster {
  subcategoryCode: string;
  items: ClassifiedExpense[];
  state: 'candidate' | 'emerging' | 'active' | 'completed';
}

function clusterMoments(rows: ClassifiedExpense[], now: Date): Cluster[] {
  const bySub = new Map<string, ClassifiedExpense[]>();
  for (const row of rows) {
    const list = bySub.get(row.subcategoryCode) ?? [];
    list.push(row);
    bySub.set(row.subcategoryCode, list);
  }
  const clusters: Cluster[] = [];
  for (const [subcategoryCode, items] of bySub) {
    const sorted = [...items].sort((a, b) => +new Date(a.effectiveAt) - +new Date(b.effectiveAt));
    const latest = new Date(sorted[sorted.length - 1].effectiveAt);
    const windowStart = now.getTime() - MOMENT_DETECTION.windowDays * 86400000;
    const recent = sorted.filter((i) => +new Date(i.effectiveAt) >= windowStart);
    const trivial = (TRIVIAL_SUBCATEGORIES as readonly string[]).includes(subcategoryCode);
    const sharedNote = sharedStoryNote(sorted);
    const promotable = !trivial || sharedNote;
    const spanDays =
      (+new Date(sorted[sorted.length - 1].effectiveAt) - +new Date(sorted[0].effectiveAt)) / 86400000;
    const quietDays = (now.getTime() - +latest) / 86400000;
    let state: Cluster['state'] = 'candidate';
    if (promotable && recent.length >= MOMENT_DETECTION.activeMinCount && spanDays >= MOMENT_DETECTION.activeMinSpanDays) {
      state = quietDays >= MOMENT_DETECTION.completedQuietDays ? 'completed' : 'active';
    } else if (promotable && recent.length >= MOMENT_DETECTION.emergingMinCount) {
      state = 'emerging';
    } else if (
      promotable &&
      sorted.length >= MOMENT_DETECTION.activeMinCount &&
      quietDays >= MOMENT_DETECTION.completedQuietDays
    ) {
      state = 'completed';
    }
    if (state !== 'candidate') clusters.push({ subcategoryCode, items: sorted, state });
  }
  return clusters;
}

function sharedNoteText(items: NetExpense[]): string | null {
  const counts = new Map<string, { count: number; text: string }>();
  for (const item of items) {
    const text = (item.note ?? '').trim();
    if (text.length < 4) continue;
    const key = text.toLowerCase();
    const prev = counts.get(key);
    counts.set(key, { count: (prev?.count ?? 0) + 1, text: prev?.text ?? text });
  }
  let best: { count: number; text: string } | null = null;
  for (const entry of counts.values()) {
    if (entry.count >= 2 && (!best || entry.count > best.count)) best = entry;
  }
  return best?.text ?? null;
}

function sharedStoryNote(items: NetExpense[]): boolean {
  return sharedNoteText(items) != null;
}

function monthWord(key: string): string {
  return monthLabel(key).split(' ')[0];
}

function lastDayOfMonth(key: string): number {
  const { year, monthIndex } = monthParts(key);
  return new Date(Date.UTC(year, monthIndex + 1, 0)).getUTCDate();
}

function memoryKicker(iso: string, currentKey: string): { kicker: string; period: 'current' | 'closed' } {
  const d = new Date(iso);
  const key = monthKey(iso);
  const period = key === currentKey ? 'current' : 'closed';
  const stamp = `${String(d.getUTCDate()).padStart(2, '0')} ${shortMonth(key).toUpperCase()} ${d.getUTCFullYear()}`;
  return { kicker: `${stamp} · ${period === 'current' ? 'CURRENT' : 'CLOSED HISTORY'}`, period };
}

function dayMonth(iso: string, padded = false): string {
  const d = new Date(iso);
  const day = padded ? String(d.getUTCDate()).padStart(2, '0') : String(d.getUTCDate());
  const mon = d.toLocaleString('en-US', { month: 'short', timeZone: 'UTC' });
  return `${day} ${mon}`;
}

function storyRange(items: NetExpense[]): string {
  const first = new Date(items[0].effectiveAt);
  const last = new Date(items[items.length - 1].effectiveAt);
  const mon = (d: Date) => d.toLocaleString('en-US', { month: 'short', timeZone: 'UTC' });
  const sameMonth = first.getUTCFullYear() === last.getUTCFullYear() && first.getUTCMonth() === last.getUTCMonth();
  const span = sameMonth
    ? `${String(first.getUTCDate()).padStart(2, '0')}–${String(last.getUTCDate()).padStart(2, '0')} ${mon(last)} ${last.getUTCFullYear()}`
    : `${first.getUTCDate()} ${mon(first)}–${last.getUTCDate()} ${mon(last)} ${last.getUTCFullYear()}`;
  return `${span} · Last activity ${dayMonth(items[items.length - 1].effectiveAt)}`;
}

function inrWords(n: number): string {
  const whole = Math.abs(n - Math.round(n)) < 0.001;
  const formatted = whole
    ? Math.round(n).toLocaleString('en-US')
    : n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return `₹${formatted}`;
}

function countLine(n: number, one: string, many: string): string | null {
  if (n <= 0) return null;
  if (n === 1) return one;
  return `${n} ${many}`;
}

function toCard(cluster: Cluster, state: MomentCard['state']): MomentCard {
  const last = cluster.items[cluster.items.length - 1];
  const spend = cluster.items.reduce((s, i) => s + i.net, 0);
  const merchant = dominantMerchant(cluster.items);
  const label = subLabel(cluster.subcategoryCode);
  const note = sharedNoteText(cluster.items);
  const title = state === 'emerging' ? `Looks like ${merchant ?? label.toLowerCase()}` : merchant ?? label;
  const countWord = cluster.items.length === 1 ? 'expense' : 'expenses';
  return {
    id: cluster.subcategoryCode,
    state,
    title,
    statusLabel:
      state === 'emerging'
        ? 'Tentative · not confirmed'
        : state === 'completed'
          ? `Completed · confirmed ${dayMonth(last.effectiveAt)}`
          : 'Active · system-derived',
    summary: note ?? `${cluster.items.length} ${countWord} in ${label}.`,
    rangeLabel: storyRange(cluster.items),
    areaCode: last.areaCode,
    areaLabel: areaLabel(last.areaCode),
    subcategoryCode: cluster.subcategoryCode,
    subcategoryLabel: label,
    spend: money(spend),
    activityCount: cluster.items.length,
    lastActivityAt: last.effectiveAt,
    artworkKey: artworkFor(cluster.subcategoryCode),
    tentative: state === 'emerging',
  };
}

function momentInsight(stories: MomentCard[]): MomentsProjection['insight'] {
  const footnote = 'Story totals reuse expenses. Nothing is added to spending again.';
  const insufficient = {
    title: null,
    text: 'Insights appear when more than one story has enough evidence.',
    sufficient: false,
    actionLabel: null,
    footnote,
  };
  const byArea = new Map<string, MomentCard[]>();
  for (const story of stories) {
    const list = byArea.get(story.areaCode) ?? [];
    list.push(story);
    byArea.set(story.areaCode, list);
  }
  const pair = [...byArea.values()].find((list) => list.length >= 2);
  if (!pair) return insufficient;
  const [a, b] = pair;
  const period = (iso: string) => new Date(iso).toLocaleString('en-US', { month: 'long', timeZone: 'UTC' });
  const total = Number(a.spend) + Number(b.spend);
  const count = a.activityCount + b.activityCount;
  return {
    title: `${a.areaLabel} connects two chapters`,
    text: `${a.title} in ${period(a.lastActivityAt)}. ${b.title} in ${period(b.lastActivityAt)}. Both belong to ${a.areaLabel}: ${count} supporting expenses, ${inrWords(total)} across two distinct periods.`,
    sufficient: true,
    actionLabel: 'Compare the two Moments',
    footnote,
  };
}

function dominantMerchant(items: NetExpense[]): string | null {
  const counts = new Map<string, number>();
  for (const i of items) {
    const name = i.merchantName?.trim();
    if (!name) continue;
    counts.set(name, (counts.get(name) ?? 0) + 1);
  }
  let best: string | null = null;
  let n = 0;
  for (const [name, count] of counts) {
    if (count > n) {
      best = name;
      n = count;
    }
  }
  return best;
}

export function projectPersonalExperience(facts: LifeExpenseFact[], now = new Date()): PersonalExperience {
  const currencyCode = pickCurrency(facts);
  const economic = economicExpenses(facts, currencyCode);
  const missingContext = economic.filter((row) => !hasLifeContext(row));
  const rows = economic.filter(hasLifeContext).sort(
    (a, b) => +new Date(b.effectiveAt) - +new Date(a.effectiveAt)
  );
  const currentKey = monthKey(now.toISOString());
  const previousKey = addMonths(currentKey, -1);
  const inMonth = (key: string) => rows.filter((r) => monthKey(r.effectiveAt) === key);
  const currentRows = inMonth(currentKey);
  const previousRows = inMonth(previousKey);
  const sum = (list: NetExpense[]) => list.reduce((s, r) => s + r.net, 0);
  const day = now.getUTCDate();
  const inWindow = (list: ClassifiedExpense[]) => list.filter((r) => new Date(r.effectiveAt).getUTCDate() <= day);
  const currentWindow = inWindow(currentRows);
  const previousWindow = inWindow(previousRows);

  const byArea = (list: ClassifiedExpense[], code: string) => list.filter((r) => r.areaCode === code);
  const topArea = PERSONAL_LIFE_AREAS.map((a) => ({
    label: a.label,
    spend: sum(byArea(currentWindow, a.code)),
  })).sort((a, b) => b.spend - a.spend)[0];

  const moodCounts = PERSONAL_MOODS.map((m) => ({
    moodCode: m.code,
    label: m.label,
    count: currentWindow.filter((r) => r.moodCode === m.code).length,
  }));
  const responseCount = moodCounts.reduce((s, m) => s + m.count, 0);

  const inferred = rows.filter((r) => r.classificationSource === 'inferred').length;
  const unclassified = inferred + missingContext.length;

  const signals = PERSONAL_LIFE_SUBCATEGORIES.flatMap((sub) => {
    const curRows = currentWindow.filter((r) => r.subcategoryCode === sub.code);
    const prevRows = previousWindow.filter((r) => r.subcategoryCode === sub.code);
    const cur = sum(curRows);
    const prev = sum(prevRows);
    if (prev <= 0 || cur <= 0) return [];
    const ratio = (cur - prev) / prev;
    if (Math.abs(ratio) < MOMENT_DETECTION.trendMaterialRatio && curRows.length === prevRows.length) return [];
    const delta = cur - prev;
    const signed = `${delta >= 0 ? '+' : '−'}${money(Math.abs(delta))}`;
    return [{
      areaLabel: areaLabel(sub.areaCode),
      subcategoryLabel: sub.label,
      current: money(cur),
      previous: money(prev),
      currentCount: curRows.length,
      previousCount: prevRows.length,
      direction: (delta >= 0 ? 'up' : 'down') as 'up' | 'down',
      sentence: `${prevRows.length} → ${curRows.length} expenses · ${signed}`,
      deltaAbs: Math.abs(delta),
    }];
  })
    .sort((a, b) => b.deltaAbs - a.deltaAbs)
    .slice(0, 2)
    .map(({ deltaAbs: _delta, ...signal }) => signal);

  const clusters = clusterMoments(rows, now);
  const active = clusters.filter((c) => c.state === 'active').slice(0, MOMENT_DETECTION.maxActive).map((c) => toCard(c, 'active'));
  const emerging = clusters.filter((c) => c.state === 'emerging').slice(0, MOMENT_DETECTION.maxEmerging).map((c) => toCard(c, 'emerging'));
  const completed = clusters.filter((c) => c.state === 'completed').map((c) => toCard(c, 'completed'));
  const progressSource = clusters.find((c) => c.state === 'active');

  const windowTotal = sum(currentWindow);
  const previousWindowTotal = sum(previousWindow);
  const comparable = previousWindow.length > 0 && currentWindow.length > 0;
  const delta = windowTotal - previousWindowTotal;
  const cold = rows.length === 0 && missingContext.length === 0;
  const monthName = monthLabel(currentKey);
  const span = daySpan(day);
  const dayWord = day === 1 ? 'day' : 'days';
  const lifeRanked = [...currentWindow.reduce((map, row) => {
    map.set(row.subcategoryCode, (map.get(row.subcategoryCode) ?? 0) + row.net);
    return map;
  }, new Map<string, number>()).entries()].sort((a, b) => b[1] - a[1]);
  const lifeTop = lifeRanked.slice(0, 5).map(([code, spend]) => ({
    subcategoryCode: code,
    label: subLabel(code),
    areaLabel: areaLabel(getPersonalLifeSubcategory(code)?.areaCode ?? ''),
    spend: money(spend),
    share: windowTotal > 0 ? spend / windowTotal : 0,
  }));
  const topFiveSpend = lifeTop.reduce((total, item) => total + Number(item.spend), 0);
  const otherSpend = Math.max(0, windowTotal - topFiveSpend);

  const nowSpend = sum(currentWindow.filter((r) => personalLifeLayerOf(r.areaCode) === 'NOW'));
  const buildSpend = sum(currentWindow.filter((r) => personalLifeLayerOf(r.areaCode) === 'BUILD'));
  const layerTotal = nowSpend + buildSpend;
  const lifeAreas = PERSONAL_LIFE_AREAS.map((a) => {
    const areaRows = byArea(currentWindow, a.code);
    const spend = sum(areaRows);
    return {
      areaCode: a.code,
      label: a.label,
      layer: a.layer,
      iconKey: a.iconKey,
      spend: money(spend),
      activityCount: areaRows.length,
      share: windowTotal > 0 ? spend / windowTotal : 0,
    };
  });
  const highlightedAreaCode = lifeAreas.reduce<{ code: string | null; spend: number }>(
    (best, area) => {
      const spend = Number(area.spend);
      return spend > best.spend ? { code: area.areaCode, spend } : best;
    },
    { code: null, spend: 0 }
  ).code;

  const storyCards = [...active, ...emerging, ...completed].sort(
    (a, b) => +new Date(b.lastActivityAt) - +new Date(a.lastActivityAt)
  );
  const timeline = storyCards.slice(0, 8).map((card) => {
    const stamped = memoryKicker(card.lastActivityAt, currentKey);
    const countWord = card.activityCount === 1 ? 'expense' : 'expenses';
    return {
      expenseId: card.id,
      title: card.title,
      effectiveAt: card.lastActivityAt,
      detail: `${card.activityCount} ${countWord} in ${card.subcategoryLabel}: ${inrWords(Number(card.spend))}.`,
      kicker: stamped.kicker,
      period: stamped.period,
    };
  });

  const closedKey = previousKey;
  const closedRows = previousRows;
  const closedStories = completed.filter((card) => monthKey(card.lastActivityAt) === closedKey);
  const closedMoods = closedRows.filter((row) => row.moodCode).length;
  const previousWord = monthWord(closedKey);
  const currentWord = monthWord(currentKey);
  const monthEnd = lastDayOfMonth(currentKey);
  const monthly = closedRows.length
    ? {
        label: monthLabel(closedKey),
        shortLabel: shortMonth(closedKey).toUpperCase(),
        year: monthParts(closedKey).year,
        closedLabel: `${monthParts(closedKey).year} · CLOSED`,
        title: closedStories[0]?.title ?? `${previousWord} expenses`,
        spend: money(sum(closedRows)),
        expenseCount: closedRows.length,
        completedCount: closedStories.length,
        moodCount: closedMoods,
        moodCoverage: closedRows.length ? closedMoods / closedRows.length : 0,
        periodCaption: `${previousWord} only`,
        summary: closedStories[0]
          ? `Includes ${closedStories[0].title}. A factual recap of ${previousWord} expenses only.`
          : `${closedRows.length} expenses across ${new Set(closedRows.map((r) => r.areaCode)).size} life areas. A factual recap of ${previousWord} expenses only.`,
        openLabel: `Open ${previousWord} memory`,
      }
    : null;

  const yearAgo = now.getTime() - MOMENT_DETECTION.lookBackDays * 86400000;
  const window = MOMENT_DETECTION.lookBackWindowDays * 86400000;
  const look = rows.find((r) => Math.abs(+new Date(r.effectiveAt) - yearAgo) <= window);
  const lookStory = look
    ? storyCards.find((card) => card.id === look.subcategoryCode) ?? null
    : null;
  const hasHistory = timeline.length > 0 || closedRows.length > 0 || look != null;

  const storiesEmpty = active.length === 0 && emerging.length === 0 && completed.length === 0;
  const insight = momentInsight(active.concat(completed));
  const progressCard = progressSource ? toCard(progressSource, 'active') : null;

  return {
    currencyCode,
    expenseCount: rows.length,
    cold,
    pulse: {
      intro: {
        kicker: cold
          ? `${monthName.toUpperCase()} · A FRESH START`
          : `${span} ${shortMonth(currentKey).toUpperCase()} ${monthParts(currentKey).year}`,
        title: cold ? 'Your everyday starts here.' : 'Your Pulse',
        subtitle: cold
          ? 'Start with one expense. Add a little life context.'
          : 'A little context. A clearer everyday.',
      },
      recent: rows.slice(0, 20).map((r) => ({
        expenseId: r.expenseId,
        merchantName: r.merchantName?.trim() || subLabel(r.subcategoryCode),
        amount: money(r.net),
        currencyCode: r.currencyCode,
        effectiveAt: r.effectiveAt,
        moodCode: r.moodCode,
        moodLabel: moodLabel(r.moodCode),
        areaCode: r.areaCode,
        areaLabel: areaLabel(r.areaCode),
        subcategoryCode: r.subcategoryCode,
        subcategoryLabel: subLabel(r.subcategoryCode),
        iconKey: iconKey(r.subcategoryCode),
      })),
      attention: attentionState(rows.length, inferred, missingContext.length),
      snapshot: {
        periodLabel: monthName,
        rangeLabel: cold ? monthName : `${monthName.split(' ')[0]} so far · ${span} ${shortMonth(currentKey)}`,
        total: money(windowTotal),
        expenseCount: currentWindow.length,
        previousTotal: previousWindow.length ? money(previousWindowTotal) : null,
        previousLabel: previousWindow.length ? monthLabel(previousKey) : null,
        previousRangeLabel: previousWindow.length ? `${span} ${shortMonth(previousKey)}` : null,
        previousExpenseCount: previousWindow.length,
        comparable,
        deltaAmount: comparable ? money(delta) : null,
        deltaRatio: comparable && previousWindowTotal > 0 ? delta / previousWindowTotal : null,
        comparisonNote: comparable
          ? `Same ${day} ${dayWord}${unclassified === 0 ? ', fully classified' : ''}. Transfers and settlements excluded.`
          : 'No prior period to compare.',
        topAreaLabel: topArea && topArea.spend > 0 ? topArea.label : null,
        topAreaAmount: topArea && topArea.spend > 0 ? money(topArea.spend) : null,
        currentShort: shortMonth(currentKey),
        previousShort: previousWindow.length ? shortMonth(previousKey) : null,
      },
      signals,
      signalsPrompt: signals.length ? `What changed in the same ${day} ${dayWord}?` : null,
      signalsMessage: signals.length ? null : 'No signals yet. Patterns need enough supporting activity.',
      mood: {
        responseCount,
        expenseCount: currentWindow.length,
        coverage: currentWindow.length ? responseCount / currentWindow.length : null,
        insufficient: responseCount < MOMENT_DETECTION.moodSampleMin,
        message:
          responseCount === 0
            ? 'Only feelings you choose to record. Never inferred.'
            : `${currentWindow.length - responseCount} expenses have no mood. Recorded responses only, not your overall emotional state.`,
        counts: moodCounts,
      },
    },
    moments: {
      intro: storiesEmpty
        ? {
            kicker: `${monthName.toUpperCase()} · A FRESH START`,
            title: 'Life happens. Stories follow.',
            subtitle: 'Your meaningful expenses can connect into Moments automatically.',
          }
        : {
            kicker: `${span} ${shortMonth(currentKey).toUpperCase()} ${monthParts(currentKey).year}`,
            title: 'Moments',
            subtitle: 'Meaningful stories, taking shape in your expenses. Found automatically, not created.',
          },
      heroMonth: storiesEmpty ? `${monthName} · a fresh start` : null,
      heroEvidence: storiesEmpty
        ? rows.length === 0
          ? '0 stories · no supporting activity'
          : '0 stories · everyday spending stays in Life'
        : null,
      activeSubtitle: countLine(active.length, 'One story unfolding', 'stories unfolding'),
      emergingSubtitle: emerging.length ? 'A possible connection, not yet confirmed' : null,
      progressSubtitle: progressCard ? 'Activity over time, not a completion score' : null,
      recentSubtitle: countLine(Math.min(completed.length, 3), 'One chapter to revisit', 'chapters to revisit'),
      active,
      emerging,
      progress: progressCard
        ? {
            title: progressCard.title,
            areaLabel: progressCard.areaLabel,
            subcategoryLabel: progressCard.subcategoryLabel,
            bars: progressSource!.items.slice(-6).map((i) => ({
              effectiveAt: i.effectiveAt,
              amount: money(i.net),
              label: dayMonth(i.effectiveAt, true),
            })),
            cumulativeLabel: `${progressCard.activityCount} expenses · ${inrWords(Number(progressCard.spend))} cumulative`,
            footnote: `Last activity ${dayMonth(progressCard.lastActivityAt)}. No goal or completion rate set.`,
            message: 'Recorded expenses. No target, so this is not a percentage.',
          }
        : null,
      recent: completed.slice(0, 3),
      insight,
    },
    life: {
      intro: cold
        ? {
            kicker: `${monthName.toUpperCase()} · A FRESH START`,
            title: 'All the parts of you.',
            subtitle: 'Eight areas of life. Explore any one, even before your first expense.',
          }
        : {
            kicker: `${span} ${shortMonth(currentKey).toUpperCase()} ${monthParts(currentKey).year}`,
            title: 'Your money, in life',
            subtitle: 'Eight areas. One connected picture.',
          },
      total: money(windowTotal),
      expenseCount: currentWindow.length,
      monthLabel: monthName,
      rangeLabel: `${span} ${shortMonth(currentKey)} · all 8 Life Areas`,
      coverageLabel: unclassified === 0 ? 'Fully classified' : `${unclassified} not yet classified`,
      highlightedAreaCode,
      areas: lifeAreas,
      nowBuild: {
        now: money(nowSpend),
        build: money(buildSpend),
        nowShare: layerTotal > 0 ? nowSpend / layerTotal : null,
        buildShare: layerTotal > 0 ? buildSpend / layerTotal : null,
      },
      moneyGoes: lifeTop,
      otherCount: Math.max(0, PERSONAL_LIFE_SUBCATEGORIES.length - lifeTop.length),
      otherSpend: money(otherSpend),
      otherShare: windowTotal > 0 ? otherSpend / windowTotal : 0,
      topFiveSpend: money(topFiveSpend),
      topFiveShare: windowTotal > 0 ? topFiveSpend / windowTotal : 0,
      trends: signals.map((s) => ({
        label: s.subcategoryLabel,
        areaLabel: s.areaLabel,
        direction: s.direction,
        detail: s.sentence,
        previous: s.previous,
        current: s.current,
        previousCount: s.previousCount,
        currentCount: s.currentCount,
        previousShort: shortMonth(previousKey),
        currentShort: shortMonth(currentKey),
      })),
      trendsMessage: signals.length ? null : 'No comparable periods yet. There’s no trend to show.',
      trendsFootnote:
        signals.length && unclassified === 0 ? `Both ${day}-day periods fully classified.` : null,
      moodSummary: `${responseCount} responses / ${currentWindow.length} expenses`,
      moodCoverageLabel: currentWindow.length
        ? `${responseCount} / ${currentWindow.length} expenses · ${Math.round((responseCount / currentWindow.length) * 100)}% coverage`
        : 'Coverage unavailable',
      moodFootnote: currentWindow.length
        ? 'Small area samples, not overall wellbeing or evidence that spending causes mood.'
        : 'Optional, explicitly recorded feelings. Area-level patterns need enough responses.',
      moodByArea: PERSONAL_LIFE_AREAS.flatMap((a) => {
        const areaRows = byArea(currentWindow, a.code).sort(
          (x, y) => +new Date(x.effectiveAt) - +new Date(y.effectiveAt)
        );
        if (!areaRows.length) return [];
        const moods = areaRows.filter((r) => r.moodCode);
        const winner = PERSONAL_MOODS.map((m) => ({
          label: m.label,
          count: moods.filter((r) => r.moodCode === (m.code as PersonalMoodCode)).length,
        })).sort((x, y) => y.count - x.count)[0];
        return [{
          areaLabel: a.label,
          moodLabel: winner && winner.count > 0 ? winner.label : null,
          responseCount: moods.length,
          expenseCount: areaRows.length,
          noMoodRecorded: moods.length === 0,
          responses: areaRows.map((r) => moodLabel(r.moodCode) ?? 'No mood'),
        }];
      }),
    },
    memory: {
      intro: hasHistory
        ? {
            kicker: `${shortMonth(currentKey).toUpperCase()} & HISTORY · ${monthParts(currentKey).year}`,
            title: 'What stays with you',
            subtitle: `The chapters behind your expenses. ${currentWord} is unfolding; ${previousWord} is closed.`,
          }
        : {
            kicker: `${monthName.toUpperCase()} · A FRESH START`,
            title: 'A story worth keeping.',
            subtitle: "There's room here for the chapters that will matter.",
          },
      openMonthShort: shortMonth(currentKey).toUpperCase(),
      openMonthYear: monthParts(currentKey).year,
      openMonthChip: `${monthParts(currentKey).year} · still open`,
      openRecap: `No closed-month history yet. A factual ${currentWord} recap can appear after ${monthEnd} ${currentWord}.`,
      openMonthNote: `${currentWord} · current. Its monthly recap appears after ${monthEnd} ${shortMonth(currentKey)}.`,
      timelineOpenKicker: `${currentWord.toUpperCase()} · OPEN`,
      currentFilterLabel: `${currentWord} · current`,
      timeline,
      completed,
      monthly,
      milestones: completed.slice(0, 3).map((c) => ({
        title: c.title,
        effectiveAt: c.lastActivityAt,
        detail: `${c.activityCount} supporting expenses · ${inrWords(Number(c.spend))}. A completed chapter, not a routine payment.`,
        areaLabel: c.areaLabel,
        subcategoryLabel: c.subcategoryLabel,
        spend: c.spend,
        activityCount: c.activityCount,
        confirmedLabel: `Confirmed by you · ${dayMonth(c.lastActivityAt)} ${new Date(c.lastActivityAt).getUTCFullYear()}`,
      })),
      lookBack: look
        ? {
            title: lookStory?.title ?? (look.merchantName?.trim() || subLabel(look.subcategoryCode)),
            detail: lookStory?.summary ?? areaLabel(look.areaCode),
            effectiveAt: look.effectiveAt,
            kicker: `${String(new Date(look.effectiveAt).getUTCDate()).padStart(2, '0')} ${shortMonth(monthKey(look.effectiveAt)).toUpperCase()} · YOUR FIRSTS`,
          }
        : null,
      lookBackMessage: look ? null : 'No earlier chapter to revisit. Memories will grow from meaningful history.',
    },
  };
}
