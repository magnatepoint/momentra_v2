BEGIN;

-- Personal life taxonomy (P1-P8 x 6) and master-expense context.
-- NOW/BUILD is derived from the life area. Mood is optional and never inferred.
-- Giving/Contributions (D-05) is intentionally absent.

CREATE TABLE personal.life_area (
    area_code TEXT PRIMARY KEY,
    layer_code TEXT NOT NULL,
    label TEXT NOT NULL,
    tagline TEXT NOT NULL,
    icon_key TEXT NOT NULL,
    tint_token TEXT NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT ck_life_area__layer CHECK (layer_code IN ('NOW', 'BUILD')),
    CONSTRAINT ck_life_area__code CHECK (area_code ~ '^P[1-8]$')
);

CREATE TABLE personal.life_subcategory (
    subcategory_code TEXT PRIMARY KEY,
    area_code TEXT NOT NULL REFERENCES personal.life_area(area_code),
    label TEXT NOT NULL,
    description TEXT NOT NULL,
    examples TEXT[] NOT NULL DEFAULT '{}',
    icon_key TEXT NOT NULL,
    sort_order INT NOT NULL,
    CONSTRAINT ck_life_subcategory__code CHECK (subcategory_code ~ '^P[1-8]\.[1-6]$')
);

CREATE TABLE personal.mood_state (
    mood_code TEXT PRIMARY KEY,
    label TEXT NOT NULL,
    icon_key TEXT NOT NULL,
    sort_order INT NOT NULL
);

ALTER TABLE finance.expense
    ADD COLUMN life_area_code TEXT,
    ADD COLUMN life_subcategory_code TEXT,
    ADD COLUMN mood_code TEXT,
    ADD COLUMN classification_source TEXT,
    ADD COLUMN origin TEXT,
    ADD COLUMN economic_kind TEXT NOT NULL DEFAULT 'EXPENSE',
    ADD COLUMN payment_source TEXT;

ALTER TABLE finance.expense
    ADD CONSTRAINT fk_expense__life_area
        FOREIGN KEY (life_area_code) REFERENCES personal.life_area(area_code),
    ADD CONSTRAINT fk_expense__life_subcategory
        FOREIGN KEY (life_subcategory_code) REFERENCES personal.life_subcategory(subcategory_code),
    ADD CONSTRAINT fk_expense__mood
        FOREIGN KEY (mood_code) REFERENCES personal.mood_state(mood_code),
    ADD CONSTRAINT ck_expense__life_context CHECK (
        (life_area_code IS NULL AND life_subcategory_code IS NULL)
        OR (life_area_code IS NOT NULL AND life_subcategory_code IS NOT NULL)
    ),
    ADD CONSTRAINT ck_expense__classification_source CHECK (
        classification_source IS NULL
        OR classification_source IN ('manual', 'suggested', 'inferred', 'corrected')
    ),
    ADD CONSTRAINT ck_expense__economic_kind CHECK (
        economic_kind IN ('EXPENSE', 'REFUND')
    ),
    ADD CONSTRAINT ck_expense__life_parent CHECK (
        life_subcategory_code IS NULL
        OR split_part(life_subcategory_code, '.', 1) = life_area_code
    );

CREATE INDEX ix_expense__personal_life
    ON finance.expense (created_by_user_id, effective_at DESC)
    WHERE domain_code = 'PERSONAL' AND life_area_code IS NOT NULL;
INSERT INTO personal.life_area (area_code, layer_code, label, tagline, icon_key, tint_token, sort_order) VALUES
  ('P1', 'NOW', 'Home & Daily Life', 'Running my everyday life', 'house', 'color.area.P1', 10),
  ('P2', 'NOW', 'Family & Care', 'Looking after the people who matter to me', 'users', 'color.area.P2', 20),
  ('P3', 'NOW', 'Health & Wellbeing', 'Looking after myself', 'heart-pulse', 'color.area.P3', 30),
  ('P4', 'NOW', 'Lifestyle & Experiences', 'Things I enjoy and experience', 'compass', 'color.area.P4', 40),
  ('P5', 'NOW', 'Shopping & Purchases', 'Things I want or need to buy', 'shopping-bag', 'color.area.P5', 50),
  ('P6', 'BUILD', 'Education & Growth', 'Learning and improving myself', 'graduation-cap', 'color.area.P6', 60),
  ('P7', 'BUILD', 'Money & Commitments', 'Managing my money and obligations', 'shield-check', 'color.area.P7', 70),
  ('P8', 'BUILD', 'Future & Milestones', 'Things I am building toward', 'flag', 'color.area.P8', 80)
ON CONFLICT (area_code) DO UPDATE SET
  layer_code = EXCLUDED.layer_code, label = EXCLUDED.label, tagline = EXCLUDED.tagline,
  icon_key = EXCLUDED.icon_key, tint_token = EXCLUDED.tint_token, sort_order = EXCLUDED.sort_order;

INSERT INTO personal.life_subcategory (subcategory_code, area_code, label, description, examples, icon_key, sort_order) VALUES
  ('P1.1', 'P1', 'Home & Housing', 'Residence, rent and living arrangements', ARRAY['Apartment rent', 'security deposit', 'move home']::text[], 'house-plus', 10),
  ('P1.2', 'P1', 'Groceries & Essentials', 'Routine food and household essentials', ARRAY['Groceries', 'toiletries', 'cleaning supplies']::text[], 'shopping-basket', 20),
  ('P1.3', 'P1', 'Bills & Utilities', 'Recurring household connectivity and utility costs', ARRAY['Electricity', 'water', 'gas', 'internet', 'mobile']::text[], 'zap', 30),
  ('P1.4', 'P1', 'Daily Mobility', 'Routine movement and local transport', ARRAY['Fuel', 'cab', 'metro', 'parking', 'tolls']::text[], 'bus', 40),
  ('P1.5', 'P1', 'Home Maintenance', 'Keeping the home and appliances functional', ARRAY['Plumber', 'repairs', 'AC/appliance service']::text[], 'wrench', 50),
  ('P1.6', 'P1', 'Daily Services', 'Routine services supporting daily living', ARRAY['Domestic help', 'laundry', 'cleaning service']::text[], 'concierge-bell', 60),
  ('P2.1', 'P2', 'Children', 'Needs and responsibilities relating to children', ARRAY['School fees', 'child activity', 'child needs']::text[], 'baby', 10),
  ('P2.2', 'P2', 'Parents & Elders', 'Care and support for parents or elders', ARRAY['Parent medicines', 'caregiver', 'elder support']::text[], 'hand-heart', 20),
  ('P2.3', 'P2', 'Partner & Family', 'Partner and immediate-family responsibilities', ARRAY['Family support', 'spouse-related household need']::text[], 'heart', 30),
  ('P2.4', 'P2', 'Dependents & Support', 'Financial support for other dependents', ARRAY['Sibling support', 'dependent relative rent']::text[], 'hand-helping', 40),
  ('P2.5', 'P2', 'Pets', 'Care and upkeep of pets', ARRAY['Pet food', 'vet', 'grooming']::text[], 'paw-print', 50),
  ('P2.6', 'P2', 'Family Occasions', 'Family-centred celebrations and occasions', ARRAY['Birthday', 'anniversary', 'family celebration']::text[], 'cake', 60),
  ('P3.1', 'P3', 'Medical & Treatment', 'Diagnosis, treatment and recovery', ARRAY['Doctor', 'hospital', 'medicine', 'dental treatment']::text[], 'stethoscope', 10),
  ('P3.2', 'P3', 'Fitness', 'Exercise, physical training and fitness activity', ARRAY['Gym', 'trainer', 'running', 'sports fitness']::text[], 'dumbbell', 20),
  ('P3.3', 'P3', 'Mental Wellbeing', 'Emotional and mental wellbeing support', ARRAY['Therapy', 'counselling', 'meditation']::text[], 'brain', 30),
  ('P3.4', 'P3', 'Personal Care', 'Personal grooming and self-care', ARRAY['Haircut', 'grooming', 'skincare treatment']::text[], 'scissors', 40),
  ('P3.5', 'P3', 'Nutrition & Wellness', 'Nutrition and broader wellness support', ARRAY['Dietician', 'nutrition program', 'wellness']::text[], 'apple', 50),
  ('P3.6', 'P3', 'Preventive Care', 'Preventive health checks and protection', ARRAY['Annual check-up', 'diagnostics', 'vaccination']::text[], 'shield-plus', 60),
  ('P4.1', 'P4', 'Travel & Holidays', 'Personal/family travel and holidays', ARRAY['Flights', 'hotel', 'holiday expenses']::text[], 'plane', 10),
  ('P4.2', 'P4', 'Dining & Going Out', 'Eating out and casual outings', ARRAY['Restaurant', 'cafe', 'dining outing']::text[], 'utensils', 20),
  ('P4.3', 'P4', 'Entertainment', 'Entertainment consumption and activities', ARRAY['Movies', 'streaming', 'games']::text[], 'clapperboard', 30),
  ('P4.4', 'P4', 'Hobbies & Interests', 'Ongoing personal interests and hobbies', ARRAY['Photography', 'music', 'hobby materials']::text[], 'palette', 40),
  ('P4.5', 'P4', 'Events & Experiences', 'Ticketed or distinct experiences', ARRAY['Concert', 'show', 'sports event']::text[], 'ticket', 50),
  ('P4.6', 'P4', 'Recreation & Social Life', 'Recreational and social activities', ARRAY['Badminton', 'club activity', 'leisure']::text[], 'party-popper', 60),
  ('P5.1', 'P5', 'Fashion & Personal Items', 'Clothing, footwear and personal accessories', ARRAY['Clothes', 'shoes', 'accessories']::text[], 'shirt', 10),
  ('P5.2', 'P5', 'Electronics & Gadgets', 'Personal electronics and technology purchases', ARRAY['Phone', 'laptop', 'smartwatch']::text[], 'smartphone', 20),
  ('P5.3', 'P5', 'Home Purchases', 'Durable items bought for the home', ARRAY['Furniture', 'refrigerator', 'appliances']::text[], 'sofa', 30),
  ('P5.4', 'P5', 'Vehicle & Mobility Purchase', 'Acquisition of personal mobility assets', ARRAY['Car', 'bike', 'bicycle']::text[], 'car', 40),
  ('P5.5', 'P5', 'Gifts', 'Items purchased specifically as gifts', ARRAY['Birthday gift', 'anniversary gift']::text[], 'gift', 50),
  ('P5.6', 'P5', 'Other Planned Purchases', 'Planned purchases not naturally covered above', ARRAY['Instrument', 'collectible', 'unusual purchase']::text[], 'package', 60),
  ('P6.1', 'P6', 'Education', 'Formal education undertaken by the user', ARRAY['University tuition', 'MBA']::text[], 'school', 10),
  ('P6.2', 'P6', 'Courses & Certifications', 'Structured courses, credentials and exams', ARRAY['AWS', 'CFA', 'certification course']::text[], 'graduation-cap', 20),
  ('P6.3', 'P6', 'Skills & Learning', 'Learning a specific capability', ARRAY['Python', 'language learning']::text[], 'puzzle', 30),
  ('P6.4', 'P6', 'Career Development', 'Professional advancement and career capability', ARRAY['Career coaching', 'leadership program']::text[], 'briefcase', 40),
  ('P6.5', 'P6', 'Books & Knowledge', 'Knowledge resources used for learning', ARRAY['Books', 'research subscription']::text[], 'library', 50),
  ('P6.6', 'P6', 'Personal Development', 'Broader self-development and capability building', ARRAY['Public speaking', 'productivity program']::text[], 'sprout', 60),
  ('P7.1', 'P7', 'Loans & EMIs', 'Repayment of borrowing obligations', ARRAY['Home-loan EMI', 'personal-loan EMI']::text[], 'landmark', 10),
  ('P7.2', 'P7', 'Insurance & Protection', 'Financial protection commitments', ARRAY['Health insurance', 'term-life premium']::text[], 'umbrella', 20),
  ('P7.3', 'P7', 'Savings', 'Money deliberately set aside', ARRAY['Emergency fund', 'reserve savings']::text[], 'piggy-bank', 30),
  ('P7.4', 'P7', 'Investments', 'Money allocated to investment assets', ARRAY['Mutual-fund SIP', 'stocks']::text[], 'trending-up', 40),
  ('P7.5', 'P7', 'Taxes & Financial Obligations', 'Tax and statutory/financial obligations', ARRAY['Income tax', 'property tax']::text[], 'file-text', 50),
  ('P7.6', 'P7', 'Recurring Commitments', 'Recurring commitments not better represented elsewhere', ARRAY['Membership', 'storage/service commitment']::text[], 'repeat', 60),
  ('P8.1', 'P8', 'Home Ownership', 'Long-term objective to buy/build a home', ARRAY['Home down payment', 'first-home plan']::text[], 'key-round', 10),
  ('P8.2', 'P8', 'Marriage & Partnership', 'Major relationship/partnership milestones', ARRAY['Wedding', 'engagement']::text[], 'gem', 20),
  ('P8.3', 'P8', 'Career Change', 'Significant transition in professional direction', ARRAY['Switch career', 'leave job for venture']::text[], 'signpost', 30),
  ('P8.4', 'P8', 'Major Life Goal', 'Significant personally defined long-term objective', ARRAY['Run marathon', 'publish a book']::text[], 'mountain', 40),
  ('P8.5', 'P8', 'Retirement & Long-term Future', 'Preparation for later-life future', ARRAY['Retirement corpus', 'retirement relocation']::text[], 'sunset', 50),
  ('P8.6', 'P8', 'Life Transition', 'Major change in living circumstances or life stage', ARRAY['Move abroad', 'independent living']::text[], 'luggage', 60)
ON CONFLICT (subcategory_code) DO UPDATE SET
  area_code = EXCLUDED.area_code, label = EXCLUDED.label, description = EXCLUDED.description,
  examples = EXCLUDED.examples, icon_key = EXCLUDED.icon_key, sort_order = EXCLUDED.sort_order;

INSERT INTO personal.mood_state (mood_code, label, icon_key, sort_order) VALUES
  ('GREAT', 'Great', 'laugh', 10),
  ('GOOD', 'Good', 'smile', 20),
  ('OKAY', 'Okay', 'meh', 30),
  ('UNEASY', 'Uneasy', 'frown', 40),
  ('BAD', 'Bad', 'angry', 50)
ON CONFLICT (mood_code) DO UPDATE SET
  label = EXCLUDED.label, icon_key = EXCLUDED.icon_key, sort_order = EXCLUDED.sort_order;

COMMIT;

