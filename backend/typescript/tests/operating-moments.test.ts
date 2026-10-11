import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  canSubmit,
  financeVisibleFor,
  needsMakerChecker,
  normalizeOperatingPreferences,
  projectOperatingMoment,
  quickAddDef,
  type OperatingEventRow,
} from '../src/modules/business/operating-catalog';

describe('operating moment catalog', () => {
  it('hides finance totals from a team member and shows them to finance', () => {
    assert.equal(financeVisibleFor('TEAM_MEMBER'), false);
    assert.equal(financeVisibleFor('FINANCE'), true);
    assert.equal(financeVisibleFor('OWNER_ADMIN'), true);
    const prefs = normalizeOperatingPreferences(
      'MARKETING_CAMPAIGNS',
      {
        subtype: 'CLIENT_CAMPAIGN',
        identityName: 'Summer launch',
        partyName: 'Northwind',
        channels: ['Instagram'],
        monthlyBudget: '10000',
        monthlyTarget: '25',
        targetMetric: 'LEADS',
      },
      { userId: 'owner-1', displayName: 'Asha' }
    );
    const events: OperatingEventRow[] = [
      {
        operatingEventId: 'e1',
        eventType: 'SPEND',
        title: 'Creator fee',
        note: null,
        amount: '2500.0000',
        currencyCode: 'INR',
        counterpartyName: null,
        channelName: 'Instagram',
        paymentRelevance: 'PAYABLE',
        paymentStatus: 'PENDING',
        approvalStatus: 'NONE',
        submittedByUserId: 'owner-1',
        decidedByUserId: null,
        occurredAt: '2026-10-01T00:00:00.000Z',
      },
    ];
    const staff = projectOperatingMoment({
      familyCode: 'MARKETING_CAMPAIGNS',
      prefs,
      role: 'TEAM_MEMBER',
      events,
    });
    const finance = projectOperatingMoment({
      familyCode: 'MARKETING_CAMPAIGNS',
      prefs,
      role: 'FINANCE',
      events,
    });
    assert.equal(staff.budget, null);
    assert.equal(staff.paymentReadiness.length, 0);
    assert.equal(finance.budget?.spent, '2500.0000');
    assert.equal(finance.budget?.remaining, '7500.0000');
    assert.equal(finance.paymentReadiness.length, 1);
    assert.equal(staff.boards.life.find((section) => section.code === 'L3')?.rows[0]?.amount, null);
    assert.equal(finance.boards.life.find((section) => section.code === 'L3')?.rows[0]?.amount, '2500.0000');
    assert.equal(staff.boards.memory.find((section) => section.code === 'ME2')?.rows[0]?.amount, null);
    assert.equal(finance.boards.memory.find((section) => section.code === 'ME2')?.rows[0]?.amount, '2500.0000');
    assert.equal(staff.quickAdds.some((q) => q.code === 'PAYMENT'), false);
    assert.equal(finance.quickAdds.some((q) => q.code === 'PAYMENT'), true);
  });

  it('requires a different approver for a contributor spend', () => {
    const spend = quickAddDef('SPEND');
    assert.ok(spend);
    assert.equal(needsMakerChecker('CONTRIBUTOR', spend), true);
    assert.equal(needsMakerChecker('FINANCE', spend), false);
    assert.equal(canSubmit('OBSERVER', spend), false);
    assert.equal(canSubmit('CONTRIBUTOR', spend), true);
  });

  it('keeps a retail store location and activation policy', () => {
    const prefs = normalizeOperatingPreferences(
      'RETAIL_CONSUMER',
      {
        subtype: 'RETAIL_STORE',
        identityName: 'Store',
        partyName: 'Store',
        monthlyBudget: '1200',
        monthlyTarget: '3000',
        targetMetric: 'REVENUE',
        location: 'Market road',
        activationPolicy: 'APPROVAL_REQUIRED',
        channels: ['Store'],
      },
      { userId: 'u', displayName: 'Asha' }
    );
    assert.equal(prefs.location, 'Market road');
    assert.equal(prefs.activationPolicy, 'APPROVAL_REQUIRED');
    assert.deepEqual(prefs.channels, ['Store']);
  });

  it('keeps restaurant and consumer service subtype, channels, location, and activation policy', () => {
    const restaurant = normalizeOperatingPreferences(
      'RETAIL_CONSUMER',
      {
        subtype: 'RESTAURANT_FOOD',
        identityName: 'Kitchen',
        partyName: 'Kitchen',
        monthlyBudget: '1200',
        monthlyTarget: '3000',
        targetMetric: 'REVENUE',
        location: 'Market road',
        activationPolicy: 'APPROVAL_REQUIRED',
        channels: ['Dine-in', 'Zomato'],
      },
      { userId: 'u', displayName: 'Asha' }
    );
    assert.equal(restaurant.subtype, 'RESTAURANT_FOOD');
    assert.equal(restaurant.location, 'Market road');
    assert.equal(restaurant.activationPolicy, 'APPROVAL_REQUIRED');
    assert.deepEqual(restaurant.channels, ['Dine-in', 'Zomato']);

    const service = normalizeOperatingPreferences(
      'RETAIL_CONSUMER',
      {
        subtype: 'CONSUMER_SERVICE',
        identityName: 'Care',
        partyName: 'Care',
        monthlyBudget: '800',
        monthlyTarget: '2000',
        targetMetric: 'REVENUE',
        location: 'Lane 4',
        activationPolicy: 'NONE',
        channels: ['Walk-in', 'Home service'],
      },
      { userId: 'u', displayName: 'Asha' }
    );
    assert.equal(service.subtype, 'CONSUMER_SERVICE');
    assert.equal(service.location, 'Lane 4');
    assert.equal(service.activationPolicy, 'NONE');
    assert.deepEqual(service.channels, ['Walk-in', 'Home service']);
  });

  it('projects a retail sale channel and hides amounts from staff', () => {
    const prefs = normalizeOperatingPreferences(
      'RETAIL_CONSUMER',
      {
        subtype: 'RETAIL_STORE',
        identityName: 'Store',
        partyName: 'Store',
        monthlyBudget: '10000',
        monthlyTarget: '30000',
        targetMetric: 'REVENUE',
        channels: ['Store'],
        startDate: '2026-10-01',
      },
      { userId: 'u', displayName: 'Asha' }
    );
    const events: OperatingEventRow[] = [
      {
        operatingEventId: 'sale-1',
        eventType: 'SALE',
        title: 'Counter sale',
        note: null,
        amount: '1800.0000',
        currencyCode: 'INR',
        counterpartyName: null,
        channelName: 'Store',
        paymentRelevance: 'RECEIVABLE',
        paymentStatus: 'PENDING',
        approvalStatus: 'NONE',
        submittedByUserId: 'u',
        decidedByUserId: null,
        occurredAt: '2026-10-02T00:00:00.000Z',
      },
      {
        operatingEventId: 'issue-1',
        eventType: 'ISSUE',
        title: 'Shelf low',
        note: 'Front row',
        amount: null,
        currencyCode: 'INR',
        counterpartyName: null,
        channelName: null,
        paymentRelevance: 'NONE',
        paymentStatus: 'NONE',
        approvalStatus: 'PENDING',
        submittedByUserId: 'u',
        decidedByUserId: null,
        occurredAt: '2026-10-03T00:00:00.000Z',
      },
    ];
    const finance = projectOperatingMoment({ familyCode: 'RETAIL_CONSUMER', prefs, role: 'FINANCE', events });
    const staff = projectOperatingMoment({ familyCode: 'RETAIL_CONSUMER', prefs, role: 'TEAM_MEMBER', events });
    const sale = finance.boards.life.find((section) => section.code === 'L3')?.rows[0];
    assert.equal(sale?.channelName, 'Store');
    assert.equal(sale?.amount, '1800.0000');
    assert.equal(finance.finance?.sales, '1800.0000');
    assert.equal(finance.finance?.outstanding, '1800.0000');
    assert.equal(finance.attention.some((item) => item.id === 'issue-1' && item.kind === 'APPROVAL'), true);
    assert.equal(staff.finance, null);
    assert.equal(staff.activity.find((row) => row.id === 'sale-1')?.amount, null);
    assert.equal(staff.boards.life.find((section) => section.code === 'L3')?.rows[0]?.amount, null);
  });

  it('counts a recorded service in sales and a collected service in received', () => {
    const prefs = normalizeOperatingPreferences(
      'RETAIL_CONSUMER',
      {
        subtype: 'CONSUMER_SERVICE',
        identityName: 'Care',
        partyName: 'Care',
        monthlyBudget: '8000',
        monthlyTarget: '20000',
        targetMetric: 'REVENUE',
        channels: ['Walk-in'],
        startDate: '2026-10-01',
      },
      { userId: 'u', displayName: 'Asha' }
    );
    const events: OperatingEventRow[] = [
      {
        operatingEventId: 'svc-1',
        eventType: 'SERVICE',
        title: 'Home visit',
        note: null,
        amount: '900.0000',
        currencyCode: 'INR',
        counterpartyName: null,
        channelName: 'Walk-in',
        paymentRelevance: 'RECEIVABLE',
        paymentStatus: 'COLLECTED',
        approvalStatus: 'NONE',
        submittedByUserId: 'u',
        decidedByUserId: null,
        occurredAt: '2026-10-02T00:00:00.000Z',
      },
      {
        operatingEventId: 'svc-2',
        eventType: 'SERVICE',
        title: 'Follow up',
        note: null,
        amount: '400.0000',
        currencyCode: 'INR',
        counterpartyName: null,
        channelName: 'Walk-in',
        paymentRelevance: 'RECEIVABLE',
        paymentStatus: 'PENDING',
        approvalStatus: 'NONE',
        submittedByUserId: 'u',
        decidedByUserId: null,
        occurredAt: '2026-10-03T00:00:00.000Z',
      },
    ];
    const finance = projectOperatingMoment({ familyCode: 'RETAIL_CONSUMER', prefs, role: 'FINANCE', events });
    assert.equal(finance.finance?.sales, '1300.0000');
    assert.equal(finance.finance?.received, '900.0000');
    assert.equal(finance.target?.progress, '1300.0000');
    assert.equal(finance.boards.life.find((section) => section.code === 'L4')?.rows.length, 2);
    assert.equal(finance.boards.moments.some((section) => section.code === 'purchases'), false);
  });

  it('rejects a retail store without a positive budget', () => {
    assert.throws(
      () =>
        normalizeOperatingPreferences(
          'RETAIL_CONSUMER',
          {
            subtype: 'RETAIL_STORE',
            identityName: 'Store',
            partyName: 'Store',
            monthlyBudget: '0',
            monthlyTarget: '10',
            targetMetric: 'REVENUE',
          },
          { userId: 'u', displayName: 'Asha' }
        ),
      /Monthly budget/
    );
  });

  it('keeps trade reach and shows a trading shipment on the dispatches board', () => {
    const prefs = normalizeOperatingPreferences(
      'TRADING_DISTRIBUTION',
      {
        subtype: 'TRADING_WHOLESALE',
        identityName: 'Trade',
        partyName: 'Trade',
        monthlyBudget: '5000',
        monthlyTarget: '12000',
        targetMetric: 'REVENUE',
        channels: ['Wholesale'],
        tradeReach: ['Domestic', 'Moon', 'Domestic'],
        counterparties: [{ name: 'Harbor', kind: 'BUYER' }],
      },
      { userId: 'u', displayName: 'Asha' }
    );
    assert.deepEqual(prefs.tradeReach, ['Domestic']);
    const events: OperatingEventRow[] = [
      {
        operatingEventId: 'ship-1',
        eventType: 'SHIPMENT',
        title: 'Dispatch',
        note: null,
        amount: null,
        currencyCode: 'INR',
        counterpartyName: null,
        channelName: 'Wholesale',
        paymentRelevance: 'NONE',
        paymentStatus: 'NONE',
        approvalStatus: 'NONE',
        submittedByUserId: 'u',
        decidedByUserId: null,
        occurredAt: '2026-10-04T00:00:00.000Z',
      },
    ];
    const view = projectOperatingMoment({ familyCode: 'TRADING_DISTRIBUTION', prefs, role: 'OWNER_ADMIN', events });
    assert.equal(view.quickAdds.some((item) => item.code === 'SHIPMENT'), true);
    const dispatches = view.boards.moments.find((section) => section.code === 'M4');
    assert.equal(dispatches?.title, 'Open Commitments');
    assert.equal(dispatches?.rows.length, 1);
    assert.equal(dispatches?.rows[0]?.title, 'Dispatch');
  });

  it('counts only campaign results toward the marketing target and offers collection on an own-business campaign', () => {
    const prefs = normalizeOperatingPreferences(
      'MARKETING_CAMPAIGNS',
      {
        subtype: 'CLIENT_CAMPAIGN',
        identityName: 'Launch',
        partyName: 'Client',
        channels: ['Email'],
        monthlyBudget: '8000',
        monthlyTarget: '1000',
        targetMetric: 'REVENUE',
      },
      { userId: 'u', displayName: 'Asha' }
    );
    const events: OperatingEventRow[] = [
      {
        operatingEventId: 'result-1',
        eventType: 'RESULT',
        title: 'Accepted result',
        note: null,
        amount: '100.0000',
        currencyCode: 'INR',
        counterpartyName: null,
        channelName: 'Email',
        paymentRelevance: 'NONE',
        paymentStatus: 'NONE',
        approvalStatus: 'NONE',
        submittedByUserId: 'u',
        decidedByUserId: null,
        occurredAt: '2026-10-04T00:00:00.000Z',
      },
      {
        operatingEventId: 'collect-1',
        eventType: 'COLLECTION',
        title: 'Cash received',
        note: null,
        amount: '50.0000',
        currencyCode: 'INR',
        counterpartyName: null,
        channelName: 'Email',
        paymentRelevance: 'COLLECT',
        paymentStatus: 'COLLECTED',
        approvalStatus: 'NONE',
        submittedByUserId: 'u',
        decidedByUserId: null,
        occurredAt: '2026-10-05T00:00:00.000Z',
      },
    ];
    const view = projectOperatingMoment({ familyCode: 'MARKETING_CAMPAIGNS', prefs, role: 'OWNER_ADMIN', events });
    assert.equal(view.target?.progress, '100.0000');
    assert.notEqual(view.finance.sales, '100.0000');
    const own = normalizeOperatingPreferences(
      'MARKETING_CAMPAIGNS',
      {
        subtype: 'OWN_BUSINESS_CAMPAIGN',
        identityName: 'Brand',
        partyName: 'Brand',
        channels: ['Instagram'],
        monthlyBudget: '8000',
        monthlyTarget: '1000',
        targetMetric: 'REVENUE',
      },
      { userId: 'u', displayName: 'Asha' }
    );
    const ownView = projectOperatingMoment({ familyCode: 'MARKETING_CAMPAIGNS', prefs: own, role: 'OWNER_ADMIN', events: [] });
    assert.equal(ownView.quickAdds.some((item) => item.code === 'COLLECTION'), true);
  });

  it('counts manufacturing progress from the selected metric and keeps collections out of revenue', () => {
    const row = (id: string, eventType: string, amount: string): OperatingEventRow => ({
      operatingEventId: id,
      eventType,
      title: eventType,
      note: null,
      amount,
      currencyCode: 'INR',
      counterpartyName: null,
      channelName: null,
      paymentRelevance: 'NONE',
      paymentStatus: 'NONE',
      approvalStatus: 'NONE',
      submittedByUserId: 'u',
      decidedByUserId: null,
      occurredAt: '2026-10-04T00:00:00.000Z',
    });
    const base = {
      subtype: 'STANDARD_PRODUCTION',
      identityName: 'Factory',
      partyName: 'Plant',
      monthlyBudget: '8000',
      monthlyTarget: '1000',
      targetMetric: 'REVENUE',
    };
    const events = [row('sale', 'SALE', '100.0000'), row('collect', 'COLLECTION', '50.0000'), row('made', 'PRODUCTION', '40.0000')];
    const revenue = normalizeOperatingPreferences('MANUFACTURING_PRODUCTION', base, { userId: 'u', displayName: 'Asha' });
    const revenueView = projectOperatingMoment({ familyCode: 'MANUFACTURING_PRODUCTION', prefs: revenue, role: 'OWNER_ADMIN', events });
    assert.equal(revenueView.target?.progress, '100.0000');
    assert.equal(revenueView.finance.sales, '100.0000');
    const output = normalizeOperatingPreferences(
      'MANUFACTURING_PRODUCTION',
      { ...base, targetMetric: 'PRODUCTION_OUTPUT' },
      { userId: 'u', displayName: 'Asha' }
    );
    const outputView = projectOperatingMoment({ familyCode: 'MANUFACTURING_PRODUCTION', prefs: output, role: 'OWNER_ADMIN', events });
    assert.equal(outputView.target?.progress, '40.0000');
    assert.equal(outputView.finance.sales, '100.0000');
    assert.equal(revenueView.quickAdds.some((item) => item.code === 'CUSTOMER_ORDER'), true);
    assert.equal(revenueView.quickAdds.some((item) => item.code === 'STOCK'), true);
    const orders = normalizeOperatingPreferences(
      'MANUFACTURING_PRODUCTION',
      {
        subtype: 'MADE_TO_ORDER',
        identityName: 'Workshop',
        partyName: 'Workshop',
        monthlyBudget: '8000',
        monthlyTarget: '10',
        targetMetric: 'ORDERS',
      },
      { userId: 'u', displayName: 'Asha' }
    );
    const orderView = projectOperatingMoment({
      familyCode: 'MANUFACTURING_PRODUCTION',
      prefs: orders,
      role: 'OWNER_ADMIN',
      events: [row('order', 'CUSTOMER_ORDER', '250.0000')],
    });
    assert.equal(orderView.target?.progress, '1.0000');
    assert.equal(orderView.quickAdds.some((item) => item.code === 'STOCK'), true);
  });

  it('rejects a client campaign without a channel', () => {
    assert.throws(
      () =>
        normalizeOperatingPreferences(
          'RETAIL_CONSUMER',
          {
            subtype: 'CLIENT_CAMPAIGN',
            identityName: 'Nope',
            partyName: 'Nope',
            monthlyBudget: '10',
            monthlyTarget: '10',
            targetMetric: 'REVENUE',
          },
          { userId: 'u', displayName: 'U' }
        ),
      /Unknown subtype/
    );
  });
});
