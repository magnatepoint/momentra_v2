import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { projectPersonalExperience, type LifeExpenseFact } from '../src/modules/personal/experience-projection';

function fact(partial: Partial<LifeExpenseFact> & Pick<LifeExpenseFact, 'expenseId' | 'subcategoryCode' | 'areaCode' | 'effectiveAt'>): LifeExpenseFact {
  return {
    amount: 100,
    currencyCode: 'INR',
    merchantName: 'Shop',
    note: null,
    moodCode: null,
    classificationSource: 'manual',
    status: 'POSTED',
    origin: 'QUICK_ADD',
    economicKind: 'EXPENSE',
    reversedExpenseId: null,
    ...partial,
  };
}

const now = new Date('2026-10-09T00:00:00.000Z');

describe('personal experience projection', () => {
  it('keeps five grocery expenses out of Moments', () => {
    const rows = [1, 2, 3, 4, 5].map((n) =>
      fact({
        expenseId: `g${n}`,
        areaCode: 'P1',
        subcategoryCode: 'P1.2',
        effectiveAt: `2026-10-0${n}T00:00:00.000Z`,
        merchantName: 'Anand Bazaar',
      })
    );
    const view = projectPersonalExperience(rows, now);
    assert.equal(view.moments.active.length, 0);
    assert.equal(view.moments.emerging.length, 0);
    assert.equal(view.moments.intro.title, 'Life happens. Stories follow.');
    assert.equal(view.moments.heroEvidence, '0 stories · everyday spending stays in Life');
    assert.equal(view.pulse.recent.length, 5);
    assert.equal(view.life.areas.length, 8);
    assert.equal(view.life.intro.title, 'Your money, in life');
    assert.equal(view.life.highlightedAreaCode, 'P1');
    assert.equal(view.life.moneyGoes[0].areaLabel, 'Home & Daily Life');
    assert.equal(view.life.areas.find((a) => a.areaCode === 'P1')?.share, 1);
    assert.equal(view.memory.intro.title, 'A story worth keeping.');
    assert.equal(view.memory.timeline.length, 0);
    assert.equal(view.memory.monthly, null);
    assert.equal(view.pulse.mood.message, 'Only feelings you choose to record. Never inferred.');
    assert.equal(view.pulse.mood.counts.length, 5);
    assert.equal(view.pulse.snapshot.topAreaAmount, '500.00');
  });

  it('forms an active fitness moment and reduces net spend for a linked refund', () => {
    const rows: LifeExpenseFact[] = [
      fact({ expenseId: 'f1', areaCode: 'P3', subcategoryCode: 'P3.2', amount: 1200, merchantName: 'Studio Fit', effectiveAt: '2026-09-20T00:00:00.000Z', moodCode: 'GOOD' }),
      fact({ expenseId: 'f2', areaCode: 'P3', subcategoryCode: 'P3.2', amount: 1200, merchantName: 'Studio Fit', effectiveAt: '2026-09-28T00:00:00.000Z', moodCode: 'GOOD' }),
      fact({ expenseId: 'f3', areaCode: 'P3', subcategoryCode: 'P3.2', amount: 1200, merchantName: 'Studio Fit', effectiveAt: '2026-10-06T00:00:00.000Z', moodCode: 'GOOD' }),
      fact({
        expenseId: 'r1',
        areaCode: 'P3',
        subcategoryCode: 'P3.2',
        amount: 200,
        economicKind: 'REFUND',
        reversedExpenseId: 'f3',
        effectiveAt: '2026-10-07T00:00:00.000Z',
      }),
    ];
    const view = projectPersonalExperience(rows, now);
    assert.equal(view.moments.active.length, 1);
    assert.equal(view.moments.active[0].title, 'Studio Fit');
    assert.equal(view.moments.active[0].statusLabel, 'Active · system-derived');
    assert.equal(view.moments.active[0].summary, '3 expenses in Fitness.');
    assert.equal(view.moments.active[0].rangeLabel, '20 Sep–6 Oct 2026 · Last activity 6 Oct');
    assert.equal(view.moments.intro.title, 'Moments');
    assert.equal(view.moments.activeSubtitle, 'One story unfolding');
    assert.equal(view.moments.progress?.bars.length, 3);
    assert.equal(view.moments.progress?.bars[2].label, '06 Oct');
    assert.equal(view.moments.progress?.cumulativeLabel, '3 expenses · ₹3,400 cumulative');
    assert.equal(view.moments.progress?.areaLabel, 'Health & Wellbeing');
    const latest = view.pulse.recent.find((r) => r.expenseId === 'f3');
    assert.equal(latest?.amount, '1000.00');
    assert.equal(view.pulse.attention.quiet, true);
    assert.equal(view.memory.intro.title, 'What stays with you');
    assert.equal(view.memory.timeline.length, 1);
    assert.equal(view.memory.timeline[0].kicker, '06 OCT 2026 · CURRENT');
    assert.equal(view.memory.timeline[0].detail, '3 expenses in Fitness: ₹3,400.');
    assert.equal(view.memory.monthly?.shortLabel, 'SEP');
    assert.equal(view.memory.monthly?.spend, '2400.00');
    assert.equal(view.memory.monthly?.expenseCount, 2);
    assert.equal(view.memory.monthly?.title, 'September expenses');
    assert.equal(view.memory.monthly?.moodCount, 2);
    assert.equal(view.memory.openMonthNote, 'October · current. Its monthly recap appears after 31 Oct.');
  });

  it('does not invent a trend or a look-back without history', () => {
    const view = projectPersonalExperience([], now);
    assert.equal(view.cold, true);
    assert.equal(view.pulse.snapshot.comparable, false);
    assert.equal(view.pulse.snapshot.total, '0.00');
    assert.equal(view.pulse.signalsMessage, 'No signals yet. Patterns need enough supporting activity.');
    assert.equal(view.pulse.intro.title, 'Your everyday starts here.');
    assert.equal(view.moments.intro.title, 'Life happens. Stories follow.');
    assert.equal(view.moments.heroEvidence, '0 stories · no supporting activity');
    assert.equal(view.pulse.attention.title, "All 0 classified. You're clear.");
    assert.equal(view.pulse.snapshot.topAreaAmount, null);
    assert.equal(view.pulse.mood.counts.length, 5);
    assert.equal(view.memory.lookBack, null);
    assert.equal(view.memory.intro.title, 'A story worth keeping.');
    assert.equal(view.memory.intro.kicker, 'OCTOBER 2026 · A FRESH START');
    assert.equal(view.memory.openMonthShort, 'OCT');
    assert.equal(view.memory.openRecap, 'No closed-month history yet. A factual October recap can appear after 31 October.');
    assert.equal(view.life.nowBuild.nowShare, null);
    assert.equal(view.life.intro.title, 'All the parts of you.');
    assert.equal(view.life.intro.kicker, 'OCTOBER 2026 · A FRESH START');
    assert.equal(view.life.highlightedAreaCode, null);
    assert.equal(view.life.moodCoverageLabel, 'Coverage unavailable');
    assert.equal(view.life.trends.length, 0);
    assert.ok(view.life.areas.every((a) => a.spend === '0.00' && a.activityCount === 0));
  });

  it('compares a subcategory across the same days of each month', () => {
    const rows: LifeExpenseFact[] = [
      fact({ expenseId: 'h1', areaCode: 'P1', subcategoryCode: 'P1.1', amount: 3000, effectiveAt: '2026-10-02T00:00:00.000Z' }),
      fact({ expenseId: 'h0', areaCode: 'P1', subcategoryCode: 'P1.1', amount: 2000, effectiveAt: '2026-09-02T00:00:00.000Z' }),
      fact({ expenseId: 'e1', areaCode: 'P6', subcategoryCode: 'P6.1', amount: 1000, effectiveAt: '2026-10-03T00:00:00.000Z', moodCode: 'GOOD' }),
    ];
    const view = projectPersonalExperience(rows, now);
    const home = view.life.areas.find((a) => a.areaCode === 'P1');
    assert.equal(home?.spend, '3000.00');
    assert.ok(Math.abs((home?.share ?? 0) - 0.75) < 0.001);
    assert.equal(view.life.nowBuild.buildShare, 0.25);
    const trend = view.life.trends.find((t) => t.label === 'Home & Housing');
    assert.equal(trend?.areaLabel, 'Home & Daily Life');
    assert.equal(trend?.previous, '2000.00');
    assert.equal(trend?.current, '3000.00');
    assert.equal(trend?.previousCount, 1);
    assert.equal(trend?.currentCount, 1);
    assert.equal(trend?.previousShort, 'Sep');
    assert.equal(trend?.currentShort, 'Oct');
    assert.equal(view.life.moodCoverageLabel, '1 / 2 expenses · 50% coverage');
    assert.deepEqual(view.life.moodByArea.find((a) => a.areaLabel === 'Education & Growth')?.responses, ['Good']);
  });

  it('sends an expense with no Life Area to attention and keeps it out of spend and stories', () => {
    const rows: LifeExpenseFact[] = [
      fact({ expenseId: 'ok', areaCode: 'P1', subcategoryCode: 'P1.1', amount: 400, effectiveAt: '2026-10-04T00:00:00.000Z' }),
      fact({
        expenseId: 'open',
        areaCode: null,
        subcategoryCode: null,
        amount: 900,
        merchantName: 'Unknown payee',
        effectiveAt: '2026-10-05T00:00:00.000Z',
      }),
    ];
    const view = projectPersonalExperience(rows, now);
    assert.equal(view.pulse.attention.quiet, false);
    assert.equal(view.pulse.attention.issueCount, 1);
    assert.equal(view.pulse.attention.title, '1 need a Life Area.');
    assert.equal(view.pulse.snapshot.total, '400.00');
    assert.equal(view.pulse.recent.some((row) => row.expenseId === 'open'), false);
    assert.equal(view.life.areas.find((area) => area.areaCode === 'P1')?.spend, '400.00');
    assert.equal(view.life.total, '400.00');
    assert.equal(view.moments.active.length, 0);
    assert.equal(view.moments.emerging.length, 0);
    assert.equal(view.expenseCount, 1);
  });
});
