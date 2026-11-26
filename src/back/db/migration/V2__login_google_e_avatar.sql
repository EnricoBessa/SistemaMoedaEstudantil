-- Login/cadastro com Google (vincula a conta local pelo e-mail) e foto de perfil (avatar).
ALTER TABLE tb_aluno ADD COLUMN google_id VARCHAR(255);
ALTER TABLE tb_aluno ADD COLUMN foto_perfil VARCHAR(255);

ALTER TABLE tb_professor ADD COLUMN google_id VARCHAR(255);
ALTER TABLE tb_professor ADD COLUMN foto_perfil VARCHAR(255);

ALTER TABLE tb_empresa_parceira ADD COLUMN google_id VARCHAR(255);
ALTER TABLE tb_empresa_parceira ADD COLUMN foto_perfil VARCHAR(255);
