CREATE INDEX idx_dns_log_timestamp  ON dns_log(timestamp DESC);
CREATE INDEX idx_dns_log_domain     ON dns_log(domain);
CREATE INDEX idx_dns_log_status     ON dns_log(status);
CREATE INDEX idx_dns_log_client_ip  ON dns_log(client_ip);
CREATE INDEX idx_dns_log_ts_status  ON dns_log(timestamp, status);
CREATE INDEX idx_blocked_domain_cat ON blocked_domain(category_id);
CREATE INDEX idx_report_admin       ON report(admin_id);
CREATE INDEX idx_daily_stats_date   ON dns_log_daily_stats(stat_date DESC);