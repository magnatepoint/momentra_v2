import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { netsAfterContributions } from '../src/modules/projection/contribution-net';

describe('contribution settlement net', () => {
  it('reduces remaining debt and the amount someone gets back by paid contributions', () => {
    const [creditor, paidMost, paidSome, overpaid] = netsAfterContributions([
      {
        participantId: 'pavan',
        currencyCode: 'INR',
        netPosition: '297305.7000',
        contributionTotal: '0',
        paidTotal: '300000.0000',
        allocatedTotal: '2694.3000',
      },
      {
        participantId: 'laxmi',
        currencyCode: 'INR',
        netPosition: '-15660.3000',
        contributionTotal: '15000.0000',
        paidTotal: '0',
        allocatedTotal: '15660.3000',
      },
      {
        participantId: 'reddy',
        currencyCode: 'INR',
        netPosition: '-15660.3000',
        contributionTotal: '7000.0000',
        paidTotal: '0',
        allocatedTotal: '15660.3000',
      },
      {
        participantId: 'ravi',
        currencyCode: 'INR',
        netPosition: '-15660.3000',
        contributionTotal: '20000.0000',
        paidTotal: '0',
        allocatedTotal: '15660.3000',
      },
    ]);

    assert.equal(paidMost.netPosition, '-660.3000');
    assert.equal(paidSome.netPosition, '-8660.3000');
    assert.equal(overpaid.netPosition, '4339.7000');
    assert.equal(creditor.netPosition, '255305.7000');
    assert.equal(creditor.paidTotal, '300000.0000');
    assert.equal(creditor.allocatedTotal, '2694.3000');
    assert.equal(paidMost.paidTotal, '0');
  });

  it('stops creditor reduction at zero and applies the largest get-back first', () => {
    const rows = netsAfterContributions([
      {
        participantId: 'small',
        currencyCode: 'INR',
        netPosition: '40.0000',
        contributionTotal: '0',
      },
      {
        participantId: 'large',
        currencyCode: 'INR',
        netPosition: '100.0000',
        contributionTotal: '0',
      },
      {
        participantId: 'payer',
        currencyCode: 'INR',
        netPosition: '-140.0000',
        contributionTotal: '120.0000',
      },
    ]);
    const byId = Object.fromEntries(rows.map((row) => [row.participantId, row.netPosition]));
    assert.equal(byId.large, '0.0000');
    assert.equal(byId.small, '20.0000');
    assert.equal(byId.payer, '-20.0000');
  });
});
