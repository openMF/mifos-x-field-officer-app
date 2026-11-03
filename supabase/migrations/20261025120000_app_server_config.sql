-- app_server_config — the Mifos server-instance list, ONE ROW PER INSTANCE.
--
-- Project lwmswhoxvvoagzkqxiyd (the shared Mifos Supabase project). Replaces reading one JSON cell
-- (`app_config.instances`) with real records: each instance is editable on its own, and each app
-- asks only for its own rows (`supported_apps @> '{field-officer}'`), so one app's servers never
-- appear in another app's picker. `app_config` is left untouched — mifos-pay still reads it until it
-- moves to this table.
--
-- Columns mirror the app_config JSON keys (snake_case), so the seed below is the live list as-is.

create table if not exists public.app_server_config (
    id                 uuid primary key default gen_random_uuid(),
    label              text        not null,
    protocol           text        not null default 'https://' check (protocol in ('https://', 'http://')),
    endpoint           text        not null,
    path               text        not null default '/fineract-provider/api/v1/',
    port               text,                                   -- null = the scheme's default
    platform_tenant_id text        not null,
    is_default         boolean     not null default false,    -- per-app default is the app's own config
    supported_apps     text[]      not null default '{}',     -- e.g. {field-officer,mifos-pay}
    demo_accounts      jsonb       not null default '[]',     -- [{role, username, password}] PUBLISHED demo sign-ins only
    interbank_servers  jsonb       not null default '[]',     -- mifos-pay's interbank list, as in app_config
    sort_order         integer     not null default 0,
    enabled            boolean     not null default true,
    created_at         timestamptz not null default now(),
    updated_at         timestamptz not null default now(),
    unique (endpoint, platform_tenant_id)
);

create index if not exists app_server_config_supported_apps_idx on public.app_server_config using gin (supported_apps);

-- Public, read-only configuration: anyone with the anon key may READ enabled rows; nobody writes
-- through the API (edits go through the dashboard / SQL editor as the owner).
alter table public.app_server_config enable row level security;
drop policy if exists "app_server_config is publicly readable" on public.app_server_config;
create policy "app_server_config is publicly readable" on public.app_server_config
    for select to anon, authenticated using (enabled);
grant select on public.app_server_config to anon, authenticated;

create or replace function public.app_server_config_touch() returns trigger language plpgsql as $$
begin new.updated_at = now(); return new; end $$;
drop trigger if exists app_server_config_touch on public.app_server_config;
create trigger app_server_config_touch before update on public.app_server_config
    for each row execute function public.app_server_config_touch();

-- Seed: app_config's four instances (unchanged, tagged mifos-pay) + the field-officer instances.
-- Re-runnable: an existing (endpoint, tenant) row is updated, not duplicated.
insert into public.app_server_config
    (label, protocol, endpoint, path, port, platform_tenant_id, is_default, supported_apps, demo_accounts, interbank_servers, sort_order)
values
    ('Mifos field officer gateway', 'https://', 'apis.mifos.community', '/1.0/field/v1/', null, 'mifos-bank-1', false,
     '{field-officer}',
     '[{"role":"Field officer","username":"fieldofficer","password":"password"},
       {"role":"Supervisor","username":"supervisor","password":"password"},
       {"role":"Capturist","username":"capturist","password":"password"},
       {"role":"Control officer","username":"controlofficer","password":"password"},
       {"role":"Treasurer","username":"treasurer","password":"password"}]',
     '[]', 10),
    ('Mifos Bank 2 Instance', 'https://', 'mifos-bank-2.mifos.community', '/fineract-provider/api/v1/', null, 'mifos-bank-2', true,
     '{mifos-pay,field-officer}',
     '[{"role":"Administrator","username":"mifos","password":"password"}]',
     '[{"path":"/v1.0/vnext2/","label":"Mifos Bank 2 Interbank","endpoint":"apis.flexcore.mx","protocol":"https://","isDefault":true}]', 20),
    ('Mifos sandbox', 'https://', 'sandbox.mifos.community', '/fineract-provider/api/v1/', null, 'default', false,
     '{field-officer}',
     '[{"role":"Administrator","username":"mifos","password":"password"}]',
     '[]', 30),
    ('Mifos Bank 1 Instance', 'https://', 'mifos-bank-1.mifos.community', '/fineract-provider/api/v1/', null, 'mifos-bank-1', false,
     '{mifos-pay}', '[]',
     '[{"path":"/v1.0/vnext1/","label":"Mifos Bank 1 Interbank","endpoint":"apis.flexcore.mx","protocol":"https://","isDefault":true}]', 40),
    ('TT Instance', 'https://', 'tt.mifos.community', '/fineract-provider/api/v1/', null, 'default', false,
     '{mifos-pay}', '[]', '[]', 50),
    ('Venus Instance', 'https://', 'venus.mifos.community', '/fineract-provider/api/v1/', null, 'venus', false,
     '{mifos-pay}', '[]', '[]', 60),
    ('Localhost', 'http://', 'localhost', '/fineract-provider/api/v1/', '8080', 'default', false,
     '{field-officer}', '[]', '[]', 90)
on conflict (endpoint, platform_tenant_id) do update set
    label = excluded.label, protocol = excluded.protocol, path = excluded.path, port = excluded.port,
    is_default = excluded.is_default, supported_apps = excluded.supported_apps,
    demo_accounts = excluded.demo_accounts, interbank_servers = excluded.interbank_servers,
    sort_order = excluded.sort_order;
