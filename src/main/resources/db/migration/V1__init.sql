CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    plan VARCHAR(20) NOT NULL CHECK (plan IN ('FREE', 'PRO', 'ENTERPRISE')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE routes (
    id UUID PRIMARY KEY, 
    tenant_id UUID NOT NULL, 
    path_pattern VARCHAR NOT NULL, 
    target_url VARCHAR NOT NULL, 
    method VARCHAR(10) NOT NULL, 
    strip_prefix BOOLEAN DEFAULT false,
    auth_type VARCHAR(20) NOT NULL CHECK (auth_type IN ('API_KEY', 'JWT', 'NONE')),
    retry_attempts INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,

    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE TABLE api_keys (
    id UUID PRIMARY KEY, 
    tenant_id UUID NOT NULL, 
    key_hash VARCHAR NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id) ON DELETE CASCADE
); 

CREATE TABLE rate_limit_policies (
    id UUID PRIMARY KEY, 
    tenant_id UUID NOT NULL UNIQUE,
    requests_per_minute INT NOT NULL,

    FOREIGN KEY (tenant_id)
    REFERENCES tenants(id) ON DELETE CASCADE
);