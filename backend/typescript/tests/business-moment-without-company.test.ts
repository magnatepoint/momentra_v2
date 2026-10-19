import assert from 'node:assert/strict';
import test from 'node:test';
import { createMomentSchema } from '../src/modules/moment/service.js';

test('a business moment can be created without a company', () => {
  const parsed = createMomentSchema.parse({
    domainCode: 'BUSINESS',
    momentTypeCode: 'RETAIL_STORE',
    title: 'Front counter',
    businessSetup: {
      familyCode: 'RETAIL_CONSUMER',
      preferences: { subtype: 'RETAIL_STORE', identityName: 'Front counter' },
    },
  });
  assert.equal(parsed.companyId, undefined);
  assert.equal(parsed.businessSetup?.familyCode, 'RETAIL_CONSUMER');
});
