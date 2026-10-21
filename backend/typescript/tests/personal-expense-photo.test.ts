import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import {
  createPersonalLifeExpenseSchema,
  decodePersonalExpensePhoto,
  updatePersonalLifeExpenseSchema,
} from '../src/modules/personal/life-expense';

const jpeg = Buffer.from([0xff, 0xd8, 0xff, 0x00, 0xd9]);

describe('personal expense photo', () => {
  it('accepts a jpeg on create and edit', () => {
    const photo = { contentType: 'image/jpeg' as const, dataBase64: jpeg.toString('base64') };
    const created = createPersonalLifeExpenseSchema.parse({
      amount: '1200',
      currencyCode: 'INR',
      lifeAreaCode: 'P3',
      lifeSubcategoryCode: 'P3.1',
      photo,
    });
    assert.equal(decodePersonalExpensePhoto(created.photo!).length, jpeg.length);
    const updated = updatePersonalLifeExpenseSchema.parse({
      lifeAreaCode: 'P3',
      lifeSubcategoryCode: 'P3.1',
      photo: null,
    });
    assert.equal(updated.photo, null);
  });

  it('rejects bytes that do not match the declared type', () => {
    assert.throws(() =>
      decodePersonalExpensePhoto({
        contentType: 'image/png',
        dataBase64: jpeg.toString('base64'),
      })
    );
  });
});
