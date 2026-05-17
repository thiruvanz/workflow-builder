CREATE USER workflow WITH PASSWORD 'workflow';
CREATE USER execution WITH PASSWORD 'execution';
CREATE USER trigger WITH PASSWORD 'trigger';

CREATE DATABASE workflowdb OWNER workflow;
CREATE DATABASE executiondb OWNER execution;
CREATE DATABASE triggerdb OWNER trigger;
