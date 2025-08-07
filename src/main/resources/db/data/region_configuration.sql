

--preconditions onFail:MARK_RAN onError:HALT
--precondition-sql-check expectedResult:1
SELECT COUNT(*) FROM region WHERE id = 'IT-32-TN';
INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('AT-07','de','https://lawinen.report','https://lawinen.report/%s','info@lawinen.report','Lawinenwarndienst Tirol','Lawinen.report');

INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('AT-02','de','https://lawinenwarndienst.ktn.gv.at','https://lawinenwarndienst.ktn.gv.at?date=%s','info@lawinen.report','Lawinenwarndienst Kärnten','Lawinenwarndienst.ktn.gv');

INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('ES-CT-L','de','https://lauegi.report','https://lauegi.report/%s','lauegi@aran.org','Centre de Lauegi Val d''Aran','Lauegi.report');

INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('EUREGIO','de','https://lawinen.report','https://lawinen.report/%s','info@lawinen.report','Lawinen.report','Lawinen.report');

INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('IT-32-BZ','de','https://lawinen.report','https://lawinen.report/%s','info@lawinen.report','Lawinenwarndienst Südtirol','Lawinen.report');

INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('IT-32-TN','de','https://lawinen.report','https://lawinen.report/%s','info@lawinen.report','Lawinenwarndienst Trentino','Lawinen.report');
