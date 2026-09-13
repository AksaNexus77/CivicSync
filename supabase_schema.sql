-- ==============================================================================
-- CivicSync Global: Production Supabase SQL Schema & Row Level Security (RLS)
-- Target Database: PostgreSQL 15+ / Supabase
-- Purpose: Secure, multi-tenant citizen caseworker data with zero data leakage
-- ==============================================================================

-- 1. Enable UUID Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. Cases Table
CREATE TABLE IF NOT EXISTS public.cases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    situation TEXT NOT NULL,
    country TEXT NOT NULL DEFAULT 'Pakistan',
    province TEXT NOT NULL DEFAULT 'Punjab',
    urgency TEXT NOT NULL DEFAULT 'Normal',
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'FILED', 'RESOLVED')),
    action_plan JSONB NOT NULL DEFAULT '{}'::jsonb,
    citizen_notes TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Indexes for lightning queries
CREATE INDEX IF NOT EXISTS idx_cases_user_id ON public.cases(user_id);
CREATE INDEX IF NOT EXISTS idx_cases_status ON public.cases(status);
CREATE INDEX IF NOT EXISTS idx_cases_created_at ON public.cases(created_at DESC);

-- Enable Row Level Security (Mandatory for Play Store / Privacy Compliance)
ALTER TABLE public.cases ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if recreating
DROP POLICY IF EXISTS "Users can only read own cases" ON public.cases;
DROP POLICY IF EXISTS "Users can insert own cases" ON public.cases;
DROP POLICY IF EXISTS "Users can update own cases" ON public.cases;
DROP POLICY IF EXISTS "Users can delete own cases" ON public.cases;

-- RLS Policies for Cases Table
CREATE POLICY "Users can only read own cases"
    ON public.cases
    FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own cases"
    ON public.cases
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own cases"
    ON public.cases
    FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own cases"
    ON public.cases
    FOR DELETE
    USING (auth.uid() = user_id);


-- 3. Documents Vault Metadata Table
CREATE TABLE IF NOT EXISTS public.documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    case_id UUID REFERENCES public.cases(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    doc_type TEXT NOT NULL DEFAULT 'OTHER',
    storage_path TEXT NOT NULL,
    mime_type TEXT NOT NULL DEFAULT 'image/jpeg',
    file_size BIGINT DEFAULT 0,
    description TEXT DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Indexes for documents
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON public.documents(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_case_id ON public.documents(case_id);

-- Enable Row Level Security
ALTER TABLE public.documents ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view own documents" ON public.documents;
DROP POLICY IF EXISTS "Users can insert own documents" ON public.documents;
DROP POLICY IF EXISTS "Users can update own documents" ON public.documents;
DROP POLICY IF EXISTS "Users can delete own documents" ON public.documents;

-- RLS Policies for Documents Table
CREATE POLICY "Users can view own documents"
    ON public.documents
    FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own documents"
    ON public.documents
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own documents"
    ON public.documents
    FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own documents"
    ON public.documents
    FOR DELETE
    USING (auth.uid() = user_id);


-- 4. Storage Bucket Configuration for 'document-vault'
-- Create private bucket for encrypted evidence & identity proof
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'document-vault',
    'document-vault',
    false,
    15728640, -- 15 MB limit per document
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'application/pdf']
)
ON CONFLICT (id) DO NOTHING;

-- Storage RLS Policies: Ensure users only access files stored under their user_id directory
DROP POLICY IF EXISTS "Users can read own vault files" ON storage.objects;
DROP POLICY IF EXISTS "Users can upload own vault files" ON storage.objects;
DROP POLICY IF EXISTS "Users can update own vault files" ON storage.objects;
DROP POLICY IF EXISTS "Users can delete own vault files" ON storage.objects;

CREATE POLICY "Users can read own vault files"
    ON storage.objects FOR SELECT
    USING (
        bucket_id = 'document-vault'
        AND (auth.uid())::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can upload own vault files"
    ON storage.objects FOR INSERT
    WITH CHECK (
        bucket_id = 'document-vault'
        AND (auth.uid())::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can update own vault files"
    ON storage.objects FOR UPDATE
    USING (
        bucket_id = 'document-vault'
        AND (auth.uid())::text = (storage.foldername(name))[1]
    );

CREATE POLICY "Users can delete own vault files"
    ON storage.objects FOR DELETE
    USING (
        bucket_id = 'document-vault'
        AND (auth.uid())::text = (storage.foldername(name))[1]
    );

-- 5. Trigger for updated_at timestamps on cases
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = timezone('utc'::text, now());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_cases_updated_at ON public.cases;
CREATE TRIGGER set_cases_updated_at
    BEFORE UPDATE ON public.cases
    FOR EACH ROW
    EXECUTE PROCEDURE public.handle_updated_at();
