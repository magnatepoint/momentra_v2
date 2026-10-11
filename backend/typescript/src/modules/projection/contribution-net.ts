import Decimal from 'decimal.js';

export interface ContributionNetPosition {
  participantId: string;
  currencyCode: string;
  netPosition: string;
  contributionTotal?: string | null;
}

function amount(value: string | null | undefined): Decimal {
  if (value == null || value === '') return new Decimal(0);
  return new Decimal(value);
}

function money(value: Decimal): string {
  return value.toFixed(4);
}

/**
 * Settlement net = expense net + recorded contribution.
 * Contributions already received reduce the people who get money back, largest expense net first,
 * and that reduction stops at zero.
 */
export function netsAfterContributions<T extends ContributionNetPosition>(positions: T[]): T[] {
  const groups = new Map<string, T[]>();
  for (const position of positions) {
    const rows = groups.get(position.currencyCode) ?? [];
    rows.push(position);
    groups.set(position.currencyCode, rows);
  }

  const adjusted = new Map<T, Decimal>();
  for (const rows of groups.values()) {
    const expenseNet = new Map<T, Decimal>();
    let pool = new Decimal(0);
    for (const row of rows) {
      const expense = amount(row.netPosition);
      const contribution = amount(row.contributionTotal);
      expenseNet.set(row, expense);
      adjusted.set(row, expense.plus(contribution));
      pool = pool.plus(contribution);
    }
    const creditors = rows
      .filter((row) => expenseNet.get(row)!.gt(0))
      .sort((a, b) => {
        const byNet = expenseNet.get(b)!.cmp(expenseNet.get(a)!);
        if (byNet !== 0) return byNet;
        return a.participantId.localeCompare(b.participantId);
      });
    for (const creditor of creditors) {
      if (pool.lte(0)) break;
      const capacity = expenseNet.get(creditor)!;
      const take = Decimal.min(pool, capacity);
      adjusted.set(creditor, adjusted.get(creditor)!.minus(take));
      pool = pool.minus(take);
    }
  }

  return positions.map((position) => ({
    ...position,
    netPosition: money(adjusted.get(position) ?? amount(position.netPosition)),
  }));
}
