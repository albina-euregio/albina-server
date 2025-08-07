-- liquibase formatted sql

-- changeset albina:007_000550-AT-07
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM regions WHERE id = 'AT-07'
INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('AT-07','de','https://lawinen.report','https://lawinen.report/%s','info@lawinen.report','Lawinenwarndienst Tirol','Lawinen.report'),
    ('AT-07','it','https://valanghe.report','https://valanghe.report/%s','info@valanghe.report','Servizio Valanghe del Tirolo','Valanghe.report'),
    ('AT-07','fr','https://avalanche.report/fr','https://avalanche.report/fr/%s',null,null,null),
    ('AT-07','es',null,null,null,'Servicio Predicción Aludes Tirol',null),
    ('AT-07','oc',null,null,null,'Servici Prediccion Lauegi Tiròl',null),
    ('AT-07','ca',null,null,null,'Servei d''alerta d''allaus del Tirol',null),
    ('AT-07','en','https://avalanche.report','https://avalanche.report/%s','info@avalanche.report','Avalanche Warning Service Tyrol','Avalanche.report');

-- changeset albina:007_000550-EUREGIO
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM regions WHERE id = 'EUREGIO'
INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('EUREGIO','de','https://lawinen.report','https://lawinen.report/%s','info@lawinen.report','Lawinen.report','Lawinen.report'),
    ('EUREGIO','it','https://valanghe.report','https://valanghe.report/%s','info@valanghe.report','Valanghe.report','Valanghe.report'),
    ('EUREGIO','fr','https://avalanche.report/fr','https://avalanche.report/fr/%s','info@avalanche.report','Avalanche.report','Avalanche.report'),
    ('EUREGIO','en','https://avalanche.report','https://avalanche.report/%s','info@avalanche.report','Avalanche.report','Avalanche.report');

-- changeset albina:007_000550-IT-32-BZ
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM regions WHERE id = 'IT-32-BZ'
INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('IT-32-BZ','de','https://lawinen.report','https://lawinen.report/%s','info@lawinen.report','Lawinenwarndienst Südtirol','Lawinen.report'),
    ('IT-32-BZ','it','https://valanghe.report','https://valanghe.report/%s','info@valanghe.report','Servizio Valanghe dell’Alto Adige','Valanghe.report'),
    ('IT-32-BZ','fr','https://avalanche.report/fr','https://avalanche.report/fr/%s','info@avalanche.report','Avalanche Warning Service South Tyrol','Avalanche.report'),
    ('IT-32-BZ','es',null,null,null,'Servicio Predicción Aludes Sud-Tirol',null),
    ('IT-32-BZ','oc',null,null,null,'Servici Prediccion Lauegi Sud-Tiròl',null),
    ('IT-32-BZ','ca',null,null,null,'Servei d''alerta d''allaus del Sud-Tirol',null),
    ('IT-32-BZ','en','https://avalanche.report','https://avalanche.report/%s','info@avalanche.report','Avalanche Warning Service South Tyrol','Avalanche.report');

-- changeset albina:007_000550-IT-32-TN
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM regions WHERE id = 'IT-32-BZ'
INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('IT-32-TN','de','https://lawinen.report','https://lawinen.report/%s','info@lawinen.report','Lawinenwarndienst Trentino','Lawinen.report'),
    ('IT-32-TN','it','https://valanghe.report','https://valanghe.report/%s','info@valanghe.report','Servizio Valanghe del Trentino','Valanghe.report'),
    ('IT-32-TN','fr','https://avalanche.report/fr','https://avalanche.report/fr/%s','info@avalanche.report','Avalanche Warning Service Trentino','Avalanche.report'),
    ('IT-32-TN','es',null,null,null,'Servicio Predicción Aludes Trentino',null),
    ('IT-32-TN','oc',null,null,null,'Servici Prediccion Lauegi Trentin',null),
    ('IT-32-TN','ca',null,null,null,'Servei d''alerta d''allaus del Trentino',null),
    ('IT-32-TN','en','https://avalanche.report','https://avalanche.report/%s','info@avalanche.report','Avalanche Warning Service Trentino','Avalanche.report');


-- changeset albina:007_000550-AT-02
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM regions WHERE id = 'AT-02'
INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('AT-02','de','https://lawinenwarndienst.ktn.gv.at','https://lawinenwarndienst.ktn.gv.at?date=%s','info@lawinen.report','Lawinenwarndienst Kärnten','Lawinenwarndienst.ktn.gv'),
    ('AT-02','it','https://lawinenwarndienst.ktn.gv.at','https://lawinenwarndienst.ktn.gv.at?date=%s','info@lawinen.report','Servizio Valanghe della Carinzia','Lawinenwarndienst.ktn.gv'),
    ('AT-02','en','https://lawinenwarndienst.ktn.gv.at','https://lawinenwarndienst.ktn.gv.at?date=%s','info@avalanche.report','Avalanche Warning Service Carinthia','Lawinenwarndienst.ktn.gv');

-- changeset albina:007_000550-ES-CT-L
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM regions WHERE id = 'ES-CT-L'
-- comment:  /* Same strings for all languages for ES-CT-L */
INSERT INTO region_language_configurations (REGION_ID,LANGUAGE_CODE,URL,URL_WITH_DATE,WARNING_SERVICE_EMAIL,WARNING_SERVICE_NAME,WEBSITE_NAME) VALUES
    ('ES-CT-L','de','https://lauegi.report','https://lauegi.report/%s','lauegi@aran.org','Centre de Lauegi Val d''Aran','Lauegi.report');

