USE master
GO

IF EXISTS (SELECT 1 FROM sys.databases WHERE name = 'bd_logym')
BEGIN
    ALTER DATABASE bd_logym SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE bd_logym;
END
GO

-- CRIAR UM BANCO DE DADOS
CREATE DATABASE bd_logym
GO
-- ACESSAR O BANCO DE DADOS
USE bd_logym
GO

/*
----------------- SELECTS -----------------
SELECT * FROM Usuario
SELECT * FROM Gerente
SELECT * FROM Academia
SELECT * FROM FotoAcademia
SELECT * FROM Favorito
SELECT * FROM Avaliacao
SELECT * FROM ItemAvaliacao
SELECT * FROM ItemAvaliacaoAcademia
SELECT * FROM Categoria
SELECT * FROM CategoriaAcademia
SELECT * FROM Facilidade;
SELECT * FROM FacilidadeAcademia;
SELECT * FROM RecuperarSenha
----------------- FIM SELECTS -----------------
*/

CREATE TABLE Usuario (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nivelAcesso VARCHAR(10) NOT NULL,
    cep CHAR(8) NULL,
    numero DECIMAL(10,0) NULL,
    complemento VARCHAR(100) NULL,
    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,
    foto VARBINARY(MAX) NULL,
    dataCadastro SMALLDATETIME NOT NULL DEFAULT GETDATE(),
    dataAtualizacao SMALLDATETIME NULL,
    statusUsuario VARCHAR(20) NOT NULL DEFAULT 'ATIVO',

    CONSTRAINT UQ_Usuario_Username UNIQUE (username),

    CONSTRAINT CK_Usuario_NivelAcesso
        CHECK (nivelAcesso IN ('ADMIN', 'MANAGER', 'USER')),

    CONSTRAINT CK_Usuario_StatusUsuario
        CHECK (statusUsuario IN ('ATIVO', 'INATIVO', 'SUSPENSO', 'TROCAR_SENHA'))
);
GO

CREATE TABLE Gerente
(
   id                INT             IDENTITY,
   nome              VARCHAR(100)    NOT NULL,
   cpf               CHAR(11)        NOT NULL,
   telefone          VARCHAR(25)     NOT NULL,
   dataNascimento    DATE            NOT NULL,
   usuario_id        INT             NOT NULL,
   dataCadastro      SMALLDATETIME   NOT NULL,
   statusGerente     VARCHAR(20)     NOT NULL, -- ATIVO ou INATIVO

   PRIMARY KEY (id),

   FOREIGN KEY (usuario_id) REFERENCES Usuario (id),

   CONSTRAINT UQ_Gerente_CPF 
   UNIQUE (cpf),

   CONSTRAINT UQ_Gerente_Usuario 
   UNIQUE (usuario_id),

   CONSTRAINT CK_Gerente_Status 
   CHECK (statusGerente IN ('ATIVO', 'INATIVO'))
);
GO

CREATE TABLE Academia (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cnpj CHAR(14) NOT NULL,
    descricao VARCHAR(400) NOT NULL,

    cep CHAR(8) NOT NULL,
    endereco VARCHAR(150) NULL,
    numero DECIMAL(10,0) NOT NULL,
    complemento VARCHAR(100) NULL,
    bairro VARCHAR(80) NULL,
    cidade VARCHAR(80) NULL,
    estado CHAR(2) NULL,

    telefone VARCHAR(25) NOT NULL,
    celular VARCHAR(25) NULL,
    email VARCHAR(100) NULL,

    latitude DECIMAL(10,7) NULL,
    longitude DECIMAL(10,7) NULL,

    categorias VARCHAR(300) NULL,
    facilidades VARCHAR(300) NULL,

    nota DECIMAL(3,1) NULL,

    gerente_id INT NOT NULL,

    dataCadastro SMALLDATETIME NOT NULL DEFAULT GETDATE(),
    statusAcademia VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    statusAnteriorBloqueioGerente VARCHAR(20) NULL,

    CONSTRAINT UQ_Academia_CNPJ UNIQUE (cnpj),

    CONSTRAINT FK_Academia_Gerente 
        FOREIGN KEY (gerente_id) REFERENCES Gerente(id),

    CONSTRAINT CK_Academia_StatusAcademia 
        CHECK (statusAcademia IN ('ATIVO', 'INATIVO', 'SUSPENSA'))
);
GO

CREATE TABLE Favorito
( 
   id				INT				IDENTITY,
   academia_id		INT				NOT NULL,
   usuario_id		INT				NOT NULL,
   dataCadastro		SMALLDATETIME	NOT NULL,
   statusFavorito	BIT				NOT NULL, -- 1 = ATIVO ou 0 = INATIVO

   PRIMARY KEY (id),
   FOREIGN KEY (usuario_id) REFERENCES Usuario (id), 
   FOREIGN KEY (academia_id) REFERENCES Academia (id)
);
GO

CREATE TABLE FotoAcademia
(
    id              INT             IDENTITY,
    foto            VARBINARY(MAX)  NOT NULL,
    tipoArquivo     VARCHAR(100)    NOT NULL,
    academia_id     INT             NOT NULL,
    dataCadastro    SMALLDATETIME   NOT NULL,
    statusFoto      VARCHAR(20)     NOT NULL, -- ATIVO ou INATIVO
    principal       BIT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    FOREIGN KEY (academia_id) REFERENCES Academia(id),

    CONSTRAINT CK_FotoAcademia_Status
    CHECK (statusFoto IN ('ATIVO', 'INATIVO'))
);
GO

CREATE UNIQUE INDEX UX_FotoAcademia_Principal
ON FotoAcademia(academia_id)
WHERE principal = 1;
GO

CREATE TABLE Categoria
( 
   id                  INT             IDENTITY,
   nome                VARCHAR(100)    NOT NULL,
   descricao           VARCHAR(300)    NULL,
   dataCriacao         SMALLDATETIME   NOT NULL DEFAULT GETDATE(),
   statusCategoria     VARCHAR(20)     NOT NULL DEFAULT 'ATIVO', -- ATIVO ou INATIVO 

   PRIMARY KEY (id),

   CONSTRAINT UQ_Categoria_Nome
   UNIQUE (nome),

   CONSTRAINT CK_Categoria_Status
   CHECK (statusCategoria IN ('ATIVO', 'INATIVO'))
);
GO

CREATE TABLE CategoriaAcademia
( 
   id                          INT             IDENTITY,
   academia_id                 INT             NOT NULL,
   categoria_id                INT             NOT NULL,
   dataCadastro                SMALLDATETIME   NOT NULL DEFAULT GETDATE(),
   observacao                  VARCHAR(200)    NULL,
   statusCategoriaAcademia     VARCHAR(20)     NOT NULL DEFAULT 'ATIVO', -- ATIVO ou INATIVO

   PRIMARY KEY (id),
   FOREIGN KEY (categoria_id) REFERENCES Categoria (id), 
   FOREIGN KEY (academia_id) REFERENCES Academia (id),

   CONSTRAINT UQ_CategoriaAcademia_Academia_Categoria
   UNIQUE (academia_id, categoria_id),

   CONSTRAINT CK_CategoriaAcademia_Status
   CHECK (statusCategoriaAcademia IN ('ATIVO', 'INATIVO'))
);
GO

CREATE TABLE Facilidade (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(300) NULL,
    statusFacilidade VARCHAR(20) NOT NULL DEFAULT 'ATIVO',

    CONSTRAINT CK_Facilidade_statusFacilidade
        CHECK (statusFacilidade IN ('ATIVO', 'INATIVO')),

    CONSTRAINT UQ_Facilidade_nome
        UNIQUE (nome)
);
GO

CREATE TABLE FacilidadeAcademia (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    academia_id INT NOT NULL,
    facilidade_id INT NOT NULL,
    statusFacilidadeAcademia VARCHAR(20) NOT NULL DEFAULT 'ATIVO',

    CONSTRAINT CK_FacilidadeAcademia_statusFacilidadeAcademia
        CHECK (statusFacilidadeAcademia IN ('ATIVO', 'INATIVO')),

    CONSTRAINT FK_FacilidadeAcademia_Academia
        FOREIGN KEY (academia_id) REFERENCES Academia(id),

    CONSTRAINT FK_FacilidadeAcademia_Facilidade
        FOREIGN KEY (facilidade_id) REFERENCES Facilidade(id),

    CONSTRAINT UQ_FacilidadeAcademia_academia_facilidade
        UNIQUE (academia_id, facilidade_id)
);
GO

CREATE TABLE Avaliacao (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    comentario  VARCHAR(400)     NULL,
    nota        DECIMAL(3,1) NOT NULL,

    academia_id INT NOT NULL,
    usuario_id  INT NOT NULL,

    dataCadastro    SMALLDATETIME   NOT NULL DEFAULT GETDATE(),
    dataAtualizacao SMALLDATETIME       NULL,
    statusAvaliacao VARCHAR(20)     NOT NULL DEFAULT 'ATIVO',

    CONSTRAINT FK_Avaliacao_Academia 
        FOREIGN KEY (academia_id) REFERENCES Academia(id),

    CONSTRAINT FK_Avaliacao_Usuario 
        FOREIGN KEY (usuario_id) REFERENCES Usuario(id),

    CONSTRAINT CK_Avaliacao_Nota 
        CHECK (nota >= 1 AND nota <= 5),

    CONSTRAINT CK_Avaliacao_StatusAvaliacao 
        CHECK (statusAvaliacao IN ('ATIVO', 'INATIVO', 'SUSPENSA')),

    CONSTRAINT UQ_Avaliacao_Usuario_Academia
        UNIQUE (usuario_id, academia_id)
);
GO

CREATE TABLE ItemAvaliacao (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    nome        VARCHAR(50)  NULL,
    descricao   VARCHAR(400) NULL
);
GO

CREATE TABLE ItemAvaliacaoAcademia (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    item_id     INT          NOT NULL,
    academia_id INT          NOT NULL,
    usuario_id  INT          NOT NULL,
    nota        DECIMAL(3,1) NOT NULL,
    statusAvaliacao BIT      DEFAULT 1,

    FOREIGN KEY(item_id) REFERENCES ItemAvaliacao (id),
    FOREIGN KEY(academia_id) REFERENCES Academia (id),
    FOREIGN KEY(usuario_id) REFERENCES Usuario(id),
    
    CONSTRAINT CK_ItemAvaliacaoAcademia_Nota 
        CHECK (nota >= 1 AND nota <= 5),

    CONSTRAINT UQ_ItemAvaliacaoAcademia_Item_Academia_Usuario
        UNIQUE (item_id, academia_id, usuario_id)
);
GO

CREATE TABLE RecuperarSenha
( 
   id				INT				IDENTITY,
   email			VARCHAR(254)	NOT NULL, -- username
   codigo			CHAR(6)			NOT NULL,
   geradoEm			SMALLDATETIME	NOT NULL DEFAULT GETDATE(),
   expiraEm		    SMALLDATETIME	NOT NULL,
   statusCodigo	    BIT				NOT NULL DEFAULT 1, -- 1 = ATIVO ou 0 = INATIVO
 
   PRIMARY KEY (id)
);
GO

----------------- INSERTS DE USUÁRIOS -----------------

---- USUÁRIO nivelAcesso=ADMIN
INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('Admin', 'admin@logym.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'ADMIN', NULL, NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
----                                        123123



---- USUÁRIO nivelAcesso=MANAGER
INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('João Pedro', 'joaopedro@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'MANAGER', NULL, NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--                                                  123123

INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario) 
VALUES ('Rodrigo Wagner', 'rodrigowagner@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'MANAGER', NULL, NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--                                                          123123

INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('Gabriel Santos', 'gabrielsantos@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'MANAGER', NULL, NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--                                                          123123

INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('Felipe Almeida', 'felipealmeida@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'MANAGER', NULL, NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--                                                          123123

INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('Bruno Martins', 'brunomartins@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'MANAGER', NULL, NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--                                                          123123



---- GERENTE
INSERT Gerente (nome, cpf, telefone, dataNascimento, usuario_id, dataCadastro, statusGerente)
VALUES ('João Pedro', '99202457042', '(11) 99999-8888', '2000-04-04', 2, GETDATE(), 'ATIVO');
--

INSERT Gerente (nome, cpf, telefone, dataNascimento, usuario_id, dataCadastro, statusGerente) 
VALUES ('Rodrigo Wagner', '48372619573', '(11) 98888-4545', '1981-08-24', 3, GETDATE(), 'ATIVO');
--

INSERT Gerente (nome, cpf, telefone, dataNascimento, usuario_id, dataCadastro, statusGerente)
VALUES ('Gabriel Santos', '71384290613', '(11) 90000-1003', '1992-05-17', 4, GETDATE(), 'ATIVO');
--

INSERT Gerente (nome, cpf, telefone, dataNascimento, usuario_id, dataCadastro, statusGerente)
VALUES ('Felipe Almeida', '52819476309', '(11) 90000-1004', '1988-11-09', 5, GETDATE(), 'ATIVO');
--

INSERT Gerente (nome, cpf, telefone, dataNascimento, usuario_id, dataCadastro, statusGerente)
VALUES ('Bruno Martins', '86130742517', '(11) 90000-1005', '1995-02-22', 6, GETDATE(), 'ATIVO');
----



---- USUÁRIO nivelAcesso=USER
INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('João Vitor', 'joaovitor@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'USER', '06401050', NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--                                                  123123
INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario) 
VALUES ('Mariana Costa', 'marianacosta@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'USER', '06401050', NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--
INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario) 
VALUES ('Lucas Ferreira', 'lucasferreira@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'USER', '06449300', NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--
INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario) 
VALUES ('Camila Rodrigues', 'camilarodrigues@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'USER', '06440180', NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
--
INSERT Usuario (nome, username, password, nivelAcesso, cep, numero, complemento, latitude, longitude, foto, dataCadastro, dataAtualizacao, statusUsuario) 
VALUES ('Rafael Almeida', 'rafaelalmeida@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'USER', '06455000', NULL, NULL, NULL, NULL, NULL, GETDATE(), NULL, 'ATIVO');
----



----------------- FIM INSERTS DE USUÁRIOS -----------------

----------------- INSERTS ITENS AVALIAÇÃO  -----------------
INSERT INTO ItemAvaliacao (nome, descricao)
VALUES
('Estrutura e equipamentos', 'Avalia a qualidade dos aparelhos, pesos, máquinas, espaço físico e estrutura geral da academia.'),
('Limpeza e organização', 'Avalia a higiene dos ambientes, banheiros, vestiários e a organização geral da academia.'),
('Atendimento', 'Avalia o atendimento da recepção, professores, funcionários e suporte oferecido aos alunos.'),
('Custo-benefício', 'Avalia se o preço cobrado é adequado em relação à estrutura, serviços e benefícios oferecidos.'),
('Localização e acessibilidade', 'Avalia a localização da academia, facilidade de acesso, estacionamento, transporte e acessibilidade.');
GO
----------------- FIM INSERTS ITENS AVALIAÇÃO  -----------------

----------------- INSERTS CATEGORIAS  -----------------
INSERT INTO Categoria (nome, descricao, dataCriacao, statusCategoria)
VALUES
('Musculação', 'Treinos com pesos, máquinas e exercícios de força.', GETDATE(), 'ATIVO'),
('Crossfit', 'Treinos funcionais de alta intensidade com exercícios variados.', GETDATE(), 'ATIVO'),
('Pilates', 'Exercícios focados em postura, flexibilidade, força e controle corporal.', GETDATE(), 'ATIVO'),
('Yoga', 'Prática voltada para equilíbrio, alongamento, respiração e bem-estar.', GETDATE(), 'ATIVO'),
('Funcional', 'Treinos com movimentos naturais do corpo, resistência e condicionamento físico.', GETDATE(), 'ATIVO'),
('Natação', 'Atividades aquáticas para condicionamento, resistência e técnica de nado.', GETDATE(), 'ATIVO'),
('Lutas', 'Modalidades de combate, defesa pessoal e artes marciais.', GETDATE(), 'ATIVO'),
('Dança', 'Aulas coletivas de dança para condicionamento, ritmo e lazer.', GETDATE(), 'ATIVO'),
('Spinning', 'Aulas em bicicleta ergométrica com foco em resistência e gasto calórico.', GETDATE(), 'ATIVO'),
('Personal Trainer', 'Atendimento individualizado com acompanhamento profissional.', GETDATE(), 'ATIVO');
GO
----------------- FIM INSERTS CATEGORIAS  -----------------

----------------- INSERTS FACILIDADES  -----------------
INSERT INTO Facilidade (nome, descricao, statusFacilidade)
VALUES
('Wi-Fi', 'Disponibiliza conexão Wi-Fi para os clientes.', 'ATIVO'),
('Estacionamento', 'Possui estacionamento disponível para os clientes.', 'ATIVO'),
('Acessibilidade', 'Possui estrutura adaptada para pessoas com deficiência ou mobilidade reduzida.', 'ATIVO'),
('Ar-condicionado', 'Ambientes de treinamento possuem climatização ou ar-condicionado.', 'ATIVO'),
('Vestiário', 'Possui vestiário disponível para utilização dos clientes.', 'ATIVO'),
('Chuveiro', 'Possui chuveiros disponíveis para utilização após os treinos.', 'ATIVO'),
('Armários', 'Possui armários para armazenamento temporário de objetos pessoais.', 'ATIVO'),
('Avaliação Física', 'Oferece serviço de avaliação física para acompanhamento dos alunos.', 'ATIVO'),
('Nutricionista', 'Possui atendimento ou acompanhamento nutricional disponível.', 'ATIVO'),
('Loja de Suplementos', 'Possui venda de suplementos ou produtos relacionados à atividade física.', 'ATIVO');
----------------- FIM INSERTS FACILIDADES  -----------------



/*
----------------- SELECTS -----------------
SELECT * FROM Usuario
SELECT * FROM Gerente
SELECT * FROM Academia
SELECT * FROM FotoAcademia
SELECT * FROM Favorito
SELECT * FROM Avaliacao
SELECT * FROM ItemAvaliacao
SELECT * FROM ItemAvaliacaoAcademia
SELECT * FROM Categoria
SELECT * FROM CategoriaAcademia
SELECT * FROM Facilidade;
SELECT * FROM FacilidadeAcademia;
SELECT * FROM RecuperarSenha
----------------- FIM SELECTS -----------------
*/

----------------- INSERTS ACADEMIAS FICTÍCIAS -----------------
INSERT INTO Academia
(
    nome, cnpj, descricao, cep, endereco, numero, complemento,
    bairro, cidade, estado, telefone, celular, email,
    latitude, longitude,
    categorias, facilidades, nota, gerente_id, dataCadastro, statusAcademia
)
VALUES
(
    'Smart Fit Barueri Centro',
    '90100000000156',
    'Academia da rede Smart Fit localizada no Centro de Barueri, com estrutura voltada à musculação, exercícios aeróbicos e diferentes modalidades de treinamento.',
    '06401050',
    'Avenida Vinte e Seis de Março',
    701,
    NULL,
    'Centro',
    'Barueri',
    'SP',
    '(11) 90000-0021',
    NULL,
    'smartfit.baruericentro@logym.com',
    -23.5111036,
    -46.8822247,
    'Musculação, Spinning',
    'Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Estrada das Pitas',
    '90100001000109',
    'Academia da rede Smart Fit localizada no Parque Viana, em Barueri, com estrutura voltada à musculação e ao condicionamento físico.',
    '06449300',
    'Estrada das Pitas',
    899,
    NULL,
    'Parque Viana',
    'Barueri',
    'SP',
    '(11) 4002-1001',
    '(11) 99999-1001',
    'smartfit.estradadaspitas@logym.com',
    -23.5474947,
    -46.8708770,
    'Musculação, Funcional',
    'Estacionamento, Ar-condicionado, Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Parque Shopping Barueri',
    '90100002000145',
    'Academia da rede Smart Fit localizada no Parque Shopping Barueri, com estrutura voltada à musculação, condicionamento físico e diferentes modalidades de treinamento.',
    '06440180',
    'Rua General de Divisão Pedro Rodrigues da Silva',
    400,
    'Parque Shopping Barueri',
    'Vila Militar',
    'Barueri',
    'SP',
    '(11) 4002-1002',
    '(11) 99999-1002',
    'smartfit.parqueshoppingbarueri@logym.com',
    -23.5163178,
    -46.8555254,
    'Musculação, Funcional',
    'Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Carrefour Hiper Tamboré',
    '90100003000190',
    'Academia da rede Smart Fit localizada na região de Alphaville e Tamboré, em Barueri, com estrutura voltada à musculação e ao condicionamento físico.',
    '06455000',
    'Alameda Araguaia',
    2751,
    'Carrefour Hiper Tamboré',
    'Alphaville Industrial',
    'Barueri',
    'SP',
    '(11) 4002-1003',
    '(11) 99999-1003',
    'smartfit.carrefourtambore@logym.com',
    -23.5012064,
    -46.8360441,
    'Musculação, Funcional',
    'Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Sodimac Alphaville',
    '90100004000134',
    'Academia da rede Smart Fit localizada no complexo do Sodimac Alphaville, em Barueri, com estrutura voltada à musculação e ao condicionamento físico.',
    '06455000',
    'Alameda Araguaia',
    1801,
    '2º Pavimento do Estacionamento Sodimac',
    'Alphaville Industrial',
    'Barueri',
    'SP',
    '(11) 4002-1004',
    '(11) 99999-1004',
    'smartfit.sodimacalphaville@logym.com',
    -23.4992055,
    -46.8439797,
    'Musculação, Funcional',
    'Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Shopping Flamingo Alphaville',
    '90100005000189',
    'Academia da rede Smart Fit localizada no Shopping Flamingo Alphaville, em Barueri, com estrutura voltada à musculação e ao condicionamento físico.',
    '06455000',
    'Alameda Araguaia',
    762,
    'Shopping Flamingo Alphaville',
    'Alphaville Industrial',
    'Barueri',
    'SP',
    '(11) 4002-1005',
    '(11) 99999-1005',
    'smartfit.shoppingflamingo@logym.com',
    -23.4989517,
    -46.8536578,
    'Musculação, Funcional',
    'Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Barueri',
    '90100006000123',
    'Academia da rede Bluefit localizada em Bethaville, em Barueri, com estrutura voltada à musculação, treinamento funcional, lutas e diferentes modalidades de condicionamento físico.',
    '06404326',
    'Avenida Trindade',
    344,
    'Loja 2023',
    'Bethaville I',
    'Barueri',
    'SP',
    '(11) 90000-0006',
    NULL,
    'bluefit.barueri@logym.com',
    -23.5061707,
    -46.8676243,
    'Musculação, Funcional, Lutas',
    'Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Alphaville',
    '90100007000178',
    'Academia da rede Bluefit localizada em Alphaville, em Barueri, com estrutura voltada à musculação, treinamento funcional e atividades de condicionamento físico.',
    '06454070',
    'Alameda Amazonas',
    388,
    NULL,
    'Alphaville Centro Industrial',
    'Barueri',
    'SP',
    '(11) 94041-1693',
    NULL,
    'bluefit.alphaville@logym.com',
    -23.4967593,
    -46.8435111,
    'Musculação, Funcional, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Roldão Osasco',
    '90100008000112',
    'Academia da rede Smart Fit localizada na Vila Quitaúna, em Osasco.',
    '06186130',
    'Rua Luiz Henrique de Oliveira',
    46,
    NULL,
    'Vila Quitaúna',
    'Osasco',
    'SP',
    '(11) 4002-1008',
    '(11) 99999-1008',
    'smartfit.roldaoosasco@logym.com',
    -23.5297000,
    -46.8151000,
    'Musculação, Funcional, Personal Trainer',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Osasco KM 18',
    '90100009000167',
    'Academia da rede Smart Fit localizada no bairro KM 18, em Osasco.',
    '06114000',
    'Rua Professor José Azevedo Minhoto',
    324,
    NULL,
    'KM 18',
    'Osasco',
    'SP',
    '(11) 4002-1009',
    '(11) 99999-1009',
    'smartfit.osascokm18@logym.com',
    -23.5293778,
    -46.7942085,
    'Musculação, Funcional, Personal Trainer',
    'Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Pátio Osasco',
    '90100010000191',
    'Academia da rede Smart Fit localizada no Centro de Osasco.',
    '06016004',
    'Rua Dona Primitiva Vianco',
    400,
    NULL,
    'Centro',
    'Osasco',
    'SP',
    '(11) 4002-1010',
    '(11) 99999-1010',
    'smartfit.patioosasco@logym.com',
    -23.5316900,
    -46.7759700,
    'Musculação, Funcional, Personal Trainer',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit União Osasco',
    '90100011000136',
    'Academia da rede Smart Fit localizada na Vila Yara, em Osasco.',
    '06020010',
    'Avenida dos Autonomistas',
    1400,
    NULL,
    'Vila Yara',
    'Osasco',
    'SP',
    '(11) 4002-1011',
    '(11) 99999-1011',
    'smartfit.uniaoosasco@logym.com',
    -23.5392810,
    -46.7655140,
    'Musculação, Funcional, Personal Trainer',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Osasco',
    '90100012000180',
    'Academia da rede Bluefit localizada no Centro de Osasco.',
    '06010065',
    'Avenida Maria Campos',
    900,
    'Loja 34',
    'Centro',
    'Osasco',
    'SP',
    '(11) 4002-1012',
    '(11) 99999-1012',
    'bluefit.osasco@logym.com',
    -23.5344561,
    -46.7731396,
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit KM 18',
    '90100013000125',
    'Academia da rede Bluefit localizada no KM 18, em Osasco.',
    '06192010',
    'Avenida Comandante Sampaio',
    685,
    'Pavimento 01',
    'KM 18',
    'Osasco',
    'SP',
    '(11) 4002-1013',
    '(11) 99999-1013',
    'bluefit.km18@logym.com',
    -23.5275433,
    -46.7923013,
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Novo Osasco',
    '90100014000170',
    'Academia da rede Bluefit localizada na região de Novo Osasco.',
    '06140040',
    'Rua Pernambucanas',
    350,
    'Loja 34',
    'Conceição',
    'Osasco',
    'SP',
    '(11) 4002-1014',
    '(11) 99999-1014',
    'bluefit.novoosasco@logym.com',
    -23.5725656,
    -46.8056043,
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Rochdale',
    '90100015000114',
    'Academia da rede Bluefit localizada no bairro Rochdale, em Osasco.',
    '06223200',
    'Rua Águas da Prata',
    246,
    NULL,
    'Rochdale',
    'Osasco',
    'SP',
    '(11) 4002-1015',
    '(11) 99999-1015',
    'bluefit.rochdale@logym.com',
    -23.5139709,
    -46.7778751,
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Carapicuíba Centro',
    '90100016000169',
    'Academia da rede Smart Fit localizada no Centro de Carapicuíba.',
    '06310240',
    'Avenida Governador Mário Covas',
    282,
    NULL,
    'Jardim Pignatary',
    'Carapicuíba',
    'SP',
    '(11) 4002-1016',
    '(11) 99999-1016',
    'smartfit.carapicuibacentro@logym.com',
    -23.5239000,
    -46.8421000,
    'Musculação, Funcional, Personal Trainer',
    'Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Vila Dirce',
    '90100017000103',
    'Academia da rede Smart Fit localizada em Carapicuíba, na região da Vila Dirce.',
    '06380021',
    'Avenida Inocêncio Seráfico',
    3445,
    NULL,
    'Vila Silva Ribeiro',
    'Carapicuíba',
    'SP',
    '(11) 4002-1017',
    '(11) 99999-1017',
    'smartfit.viladirce@logym.com',
    -23.5467000,
    -46.8339000,
    'Musculação, Funcional, Personal Trainer',
    'Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Parque Santa Teresa',
    '90100018000158',
    'Academia da rede Smart Fit localizada no Parque Santa Teresa, em Carapicuíba.',
    '06340380',
    'Rua Eduardo Augusto Mesquita',
    1147,
    NULL,
    'Parque Santa Teresa',
    'Carapicuíba',
    'SP',
    '(11) 4002-1018',
    '(11) 99999-1018',
    'smartfit.parquesantateresa@logym.com',
    -23.5661963,
    -46.8254419,
    'Musculação, Funcional, Personal Trainer',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Jandira Centro',
    '90100019000100',
    'Academia da rede Smart Fit localizada no Centro de Jandira.',
    '06600010',
    'Avenida Carmine Gragnano',
    20,
    NULL,
    'Centro',
    'Jandira',
    'SP',
    '(11) 4002-1019',
    '(11) 99999-1019',
    'smartfit.jandiracentro@logym.com',
    -23.5298289,
    -46.8979131,
    'Musculação, Funcional, Personal Trainer',
    'Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Itapevi Centro',
    '90100020000127',
    'Academia da rede Smart Fit localizada no Centro de Itapevi.',
    '06653080',
    'Rua Joaquim Nunes',
    0,
    'S/N',
    'Centro',
    'Itapevi',
    'SP',
    '(11) 4002-1020',
    '(11) 99999-1020',
    'smartfit.itapevicentro@logym.com',
    -23.5469200,
    -46.9347600,
    'Musculação, Funcional, Personal Trainer',
    'Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Carapicuíba',
    '90100021000171',
    'Academia da rede Bluefit localizada em Carapicuíba, com estrutura completa para musculação, treinos funcionais e aulas coletivas.',
    '06382260',
    'Estrada Ernestina Vieira',
    149,
    '1º Piso',
    'Vila Dirce',
    'Carapicuíba',
    'SP',
    '(11) 4002-1021',
    '(11) 99999-1021',
    'bluefit.carapicuiba@logym.com',
    -23.5519493,
    -46.8388493,
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Cotia',
    '90100022000116',
    'Academia da rede Bluefit localizada em Cotia, oferecendo estrutura para musculação, exercícios funcionais e atividades coletivas.',
    '06717210',
    'Avenida Nossa Senhora de Fátima',
    558,
    NULL,
    'Jardim Monte Santo',
    'Cotia',
    'SP',
    '(11) 4002-1022',
    '(11) 99999-1022',
    'bluefit.cotia@logym.com',
    -23.6081173,
    -46.9242644,
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Granja Viana',
    '90100023000160',
    'Academia da rede Bluefit localizada na região da Granja Viana, em Cotia, com ampla estrutura para diferentes modalidades de treino.',
    '06709150',
    'Avenida São Camilo',
    1066,
    NULL,
    'Granja Viana',
    'Cotia',
    'SP',
    '(11) 4002-1023',
    '(11) 99999-1023',
    'bluefit.granjaviana@logym.com',
    -23.5846711,
    -46.8357644,
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
);
----------------- INSERTS NOVAS ACADEMIAS DE BARUERI -----------------

INSERT INTO Academia
(
    nome, cnpj, descricao, cep, endereco, numero, complemento,
    bairro, cidade, estado, telefone, celular, email,
    latitude, longitude,
    categorias, facilidades, nota, gerente_id, dataCadastro, statusAcademia
)
VALUES

-- 1. PANOBIANCO ACADEMIA - BARUERI CENTRO
-- Gerente: João Pedro
(
    'Panobianco Academia - Barueri Centro',
    '98000001000130',
    'Academia localizada na região central de Barueri, voltada à prática de musculação, exercícios aeróbicos e atividades para diferentes perfis de alunos, com ampla faixa de horários de funcionamento.',
    '06401126',
    'Rua Jose Maria Balieiro',
    267,
    NULL,
    'Centro',
    'Barueri',
    'SP',
    '(11) 91775-7019',
    NULL,
    NULL,
    -23.5093657,
    -46.8696010,
    'Musculação, Dança, Spinning',
    'Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),

-- 2. ACADEMIA PRIMAX
-- Gerente: João Pedro
(
    'Academia Primax',
    '98000002000185',
    'Academia situada no Centro de Barueri, com ambiente destinado a musculação e condicionamento físico, atendendo alunos que buscam saúde, qualidade de vida e evolução nos treinamentos.',
    '06401050',
    'Avenida Vinte e Seis de Março',
    1331,
    NULL,
    'Centro',
    'Barueri',
    'SP',
    '(11) 93800-6025',
    NULL,
    NULL,
    -23.5099022,
    -46.8879723,
    'Musculação, Funcional, Dança',
    'Wi-Fi, Ar-condicionado, Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),

-- 3. RED FITNESS
-- Gerente: Felipe Almeida
(
    'Red Fitness',
    '98000003000120',
    'Academia localizada no Parque dos Camargos, em Barueri, com estrutura voltada à musculação, treinamento funcional, treino de força e condicionamento físico para diferentes perfis de alunos.',
    '06436000',
    'Avenida Zélia',
    902,
    NULL,
    'Parque dos Camargos',
    'Barueri',
    'SP',
    '(11) 93337-2220',
    NULL,
    NULL,
    -23.5389523,
    -46.8873503,
    'Musculação, Funcional',
    'Wi-Fi, Vestiário',
    NULL,
    4,
    GETDATE(),
    'ATIVO'
),

-- 4. HOLY SPIRIT ACADEMIA
-- Gerente: Gabriel Santos
(
    'Holy Spirit Academia',
    '98000004000174',
    'Academia localizada no Jardim Silveira, com foco em musculação, condicionamento físico e desenvolvimento de uma rotina de exercícios adequada a diferentes objetivos.',
    '06433000',
    'Avenida Mun.',
    254,
    NULL,
    'Jardim Silveira',
    'Barueri',
    'SP',
    '(11) 91065-2321',
    NULL,
    NULL,
    -23.5231974,
    -46.8916591,
    'Musculação, Funcional',
    'Wi-Fi, Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    3,
    GETDATE(),
    'ATIVO'
),

-- 5. FITNESS ACADEMY
-- Gerente: Felipe Almeida
(
    'Fitness Academy',
    '98000005000119',
    'Academia de bairro localizada no Jardim Alberto, oferecendo ambiente para treinamento físico, musculação e melhoria do condicionamento e qualidade de vida.',
    '06433220',
    'Rua Dr. Francis D Hornet',
    120,
    NULL,
    'Jardim Alberto',
    'Barueri',
    'SP',
    '(11) 96640-9164',
    NULL,
    NULL,
    -23.5218524,
    -46.8912353,
    'Musculação, Funcional',
    'Vestiário',
    NULL,
    4,
    GETDATE(),
    'ATIVO'
),

-- 6. ACADEMIA SMITHFIT
-- Gerente: Felipe Almeida
(
    'Academia SmithFit',
    '98000007000108',
    'Academia localizada no Jardim Belval, com proposta voltada a musculação, condicionamento físico e prática regular de exercícios em ambiente acessível à comunidade local.',
    '06420210',
    'Avenida Itaqui',
    346,
    NULL,
    'Jardim Belval',
    'Barueri',
    'SP',
    '(11) 99648-7393',
    NULL,
    NULL,
    -23.5084703,
    -46.8934736,
    'Musculação',
    'Wi-Fi, Vestiário',
    NULL,
    4,
    GETDATE(),
    'ATIVO'
),

-- 7. ACADEMIA EVOLUTION
-- Gerente: Felipe Almeida
(
    'Academia Evolution',
    '98000008000152',
    'Academia situada em Barueri, direcionada à prática de musculação e atividades de condicionamento físico para alunos com diferentes objetivos.',
    '06420180',
    'Rua Salgueiro',
    78,
    NULL,
    'Jardim Belval',
    'Barueri',
    'SP',
    '(11) 4163-2536',
    NULL,
    NULL,
    -23.5081682,
    -46.8947092,
    'Musculação',
    'Wi-Fi, Estacionamento, Vestiário',
    NULL,
    4,
    GETDATE(),
    'ATIVO'
),

-- 8. FLASH POINT ACADEMIA
-- Gerente: Gabriel Santos
(
    'Flash Point Academia',
    '98000009000105',
    'Academia localizada no Jardim Silveira, oferecendo espaço dedicado a musculação e condicionamento físico, com horários amplos para facilitar a rotina dos alunos.',
    '06433010',
    'Avenida Brigadeiro Manoel Rodrigues Jordão',
    552,
    NULL,
    'Jardim Silveira',
    'Barueri',
    'SP',
    '(11) 4552-5494',
    NULL,
    NULL,
    -23.5259199,
    -46.8899837,
    'Musculação, Dança',
    'Vestiário',
    NULL,
    3,
    GETDATE(),
    'ATIVO'
),

-- 9. BODYUP ACADEMIA BARUERI
-- Gerente: Bruno Martins
(
    'BodyUp Academia Barueri',
    '98000010000121',
    'Academia localizada na Vila Pindorama, voltada ao treinamento físico, musculação e desenvolvimento da saúde e do condicionamento corporal.',
    '06415000',
    'Avenida Capitão Francisco César',
    1540,
    NULL,
    'Vila Pindorama',
    'Barueri',
    'SP',
    '(11) 98416-3335',
    NULL,
    NULL,
    -23.4872198,
    -46.8940557,
    'Musculação, Funcional, Pilates, Dança, Lutas',
    'Wi-Fi, Estacionamento, Acessibilidade, Vestiário',
    NULL,
    5,
    GETDATE(),
    'ATIVO'
),

-- 10. ACADEMIA TEXFIT
-- Gerente: João Pedro
(
    'Academia Texfit',
    '98000011000176',
    'Academia situada no Centro de Barueri, oferecendo estrutura para musculação e exercícios físicos, com ampla disponibilidade de horários ao longo da semana.',
    '06401127',
    'Rua Joao Acacio de Almeida',
    153,
    NULL,
    'Centro',
    'Barueri',
    'SP',
    '(11) 94321-3876',
    NULL,
    NULL,
    -23.5087224,
    -46.8675195,
    'Musculação, Pilates, Funcional, Lutas, Dança, Spinning',
    'Wi-Fi, Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),

-- 11. SKYFIT ESTRADA DOS ROMEIROS
-- Gerente: Rodrigo Wagner
(
    'SkyFit Estrada dos Romeiros',
    '98000012000110',
    'Unidade localizada na Estrada dos Romeiros, com ambiente dedicado à musculação, atividades aeróbicas e condicionamento físico para diferentes perfis de usuários.',
    '06417000',
    'Estrada dos Romeiros',
    1565,
    NULL,
    'Vila Sao Silvestre',
    'Barueri',
    'SP',
    '(11) 97369-4005',
    NULL,
    NULL,
    -23.4928333,
    -46.8810514,
    'Musculação, Funcional, Dança',
    'Estacionamento, Acessibilidade, Vestiário',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),

-- 12. NEW POWER FITNESS ACADEMIA
-- Gerente: Bruno Martins
(
    'New Power Fitness Academia',
    '98000013000165',
    'Academia localizada no Parque Ribeiro de Lima, oferecendo espaço para musculação e atividades de condicionamento físico em ambiente direcionado à prática regular de exercícios.',
    '06405030',
    'Rua Mar do Caribe',
    51,
    'Sala 2',
    'Parque Ribeiro de Lima',
    'Barueri',
    'SP',
    '(11) 96526-2128',
    NULL,
    NULL,
    -23.5020253,
    -46.8966088,
    'Musculação, Funcional',
    'Vestiário',
    NULL,
    5,
    GETDATE(),
    'ATIVO'
),

-- 13. ACADEMIA PARA MULHERES BARUERI
-- Gerente: Gabriel Santos
(
    'Academia Para Mulheres Barueri',
    '98000014000100',
    'Academia voltada ao público feminino, localizada em Barueri, com proposta direcionada à prática de exercícios, condicionamento físico e promoção da saúde e bem-estar.',
    '06404000',
    'Rua Damião Fernandes',
    105,
    NULL,
    'Vila Srg. Jose de Paula',
    'Barueri',
    'SP',
    '(11) 2771-5663',
    NULL,
    NULL,
    -23.5097625,
    -46.8725986,
    'Funcional',
    'Vestiário',
    NULL,
    3,
    GETDATE(),
    'ATIVO'
),

-- 14. ON FORCE ACADEMIA - BARUERI
-- Gerente: Bruno Martins
(
    'ON FORCE Academia - BARUERI',
    '98000015000154',
    'Academia localizada no Jardim Regina Alice, com foco em musculação, treinamento físico e desenvolvimento de força, resistência e condicionamento corporal.',
    '06412100',
    'Rua Ver. José Viêira',
    367,
    NULL,
    'Jardim Regina Alice',
    'Barueri',
    'SP',
    '(11) 97157-7564',
    NULL,
    NULL,
    -23.5003295,
    -46.8797943,
    'Musculação, Funcional, Lutas',
    'Vestiário',
    NULL,
    5,
    GETDATE(),
    'ATIVO'
),

-- 15. ACADEMIA 24 WELLNESS - UNIDADE ALPHAVILLE
-- Gerente: Rodrigo Wagner
(
    'Academia 24 Wellness - Unidade Alphaville',
    '98000016000107',
    'Academia localizada em Alphaville Industrial, com estrutura voltada à musculação, condicionamento físico e prática regular de exercícios, oferecendo horários amplos para diferentes rotinas de treinamento.',
    '06454050',
    'Alameda Grajaú',
    525,
    NULL,
    'Alphaville Industrial',
    'Barueri',
    'SP',
    '(11) 91537-1685',
    NULL,
    NULL,
    -23.4938686,
    -46.8476774,
    'Musculação, Funcional, Lutas',
    'Estacionamento, Acessibilidade, Vestiário',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),

-- 16. HOLY SPIRIT ACADEMIA - JARDIM PAULISTA
-- Gerente: Gabriel Santos
(
    'Holy Spirit Academia - Jardim Paulista',
    '98000017000143',
    'Unidade localizada no Jardim Paulista, oferecendo ambiente destinado à musculação, condicionamento físico e treinamento para usuários com diferentes níveis de experiência.',
    '06447170',
    'Avenida Marginal Direita',
    780,
    NULL,
    'Jardim Paulista',
    'Barueri',
    'SP',
    '(11) 99376-0397',
    NULL,
    NULL,
    -23.5392539,
    -46.8782956,
    'Musculação, Funcional',
    'Wi-Fi, Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    3,
    GETDATE(),
    'ATIVO'
),

-- 17. SKYFIT ALPHAVILLE
-- Gerente: Rodrigo Wagner
(
    'Skyfit Alphaville',
    '98000018000198',
    'Academia localizada em Alphaville Industrial, com estrutura voltada à musculação, exercícios aeróbicos e condicionamento físico, atendendo diferentes perfis de alunos.',
    '06455010',
    'Avenida Juruá',
    343,
    NULL,
    'Alphaville Industrial',
    'Barueri',
    'SP',
    '(11) 98804-0056',
    NULL,
    NULL,
    -23.5009117,
    -46.8547114,
    'Musculação, Funcional, Pilates, Dança, Lutas',
    'Vestiário',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),

-- 18. IRONBERG ALPHAVILLE
-- Gerente: Rodrigo Wagner
(
    'Ironberg Alphaville',
    '98000019000132',
    'Academia localizada em Barueri, com funcionamento 24 horas e ambiente voltado à musculação e ao treinamento físico para diferentes níveis de experiência.',
    '06465100',
    'Estrada Aldeinha',
    181,
    NULL,
    'Jardim Santa Cecilia',
    'Barueri',
    'SP',
    '(11) 95558-0797',
    NULL,
    NULL,
    -23.5085641,
    -46.8477651,
    'Musculação',
    'Estacionamento, Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
),

-- 19. MISTER BIG ACADEMIA
-- Gerente: Bruno Martins
(
    'Mister Big Academia',
    '98000020000167',
    'Academia localizada no Jardim Itaparica, com espaço direcionado à musculação, condicionamento físico e manutenção de uma rotina regular de exercícios.',
    '06447020',
    'Avenida Cidade de Itu',
    140,
    NULL,
    'Jardim Itaparica',
    'Barueri',
    'SP',
    '(11) 4201-3978',
    NULL,
    NULL,
    -23.5415005,
    -46.8829314,
    'Musculação, Funcional',
    'Vestiário',
    NULL,
    5,
    GETDATE(),
    'ATIVO'
),

-- 20. ACADEMIA GAVIÕES 24H - ALPHAVILLE
-- Gerente: Rodrigo Wagner
(
    'Academia Gaviões 24h - Alphaville',
    '98000022000156',
    'Academia localizada em Alphaville, com funcionamento 24 horas e estrutura voltada à musculação, treinamento físico e diferentes modalidades de atividades para diversos perfis de alunos.',
    '06455020',
    'Avenida Juruá',
    253,
    NULL,
    'Alphaville',
    'Barueri',
    'SP',
    '(11) 94074-7584',
    NULL,
    NULL,
    -23.5005887,
    -46.8537846,
    'Musculação, Funcional, Lutas, Dança, Pilates, Spinning',
    'Acessibilidade, Ar-condicionado, Vestiário',
    NULL,
    2,
    GETDATE(),
    'ATIVO'
);

----------------- FIM INSERTS NOVAS ACADEMIAS DE BARUERI -----------------

----------------- INSERTS CATEGORIAS DAS ACADEMIAS FICTÍCIAS -----------------
INSERT INTO CategoriaAcademia (academia_id, categoria_id, dataCadastro, observacao, statusCategoriaAcademia)
SELECT
    a.id,
    c.id,
    GETDATE(),
    NULL,
    'ATIVO'
FROM Academia a
INNER JOIN Categoria c
    ON ',' + REPLACE(LOWER(a.categorias), ', ', ',') + ','
       LIKE '%,' + LOWER(c.nome) + ',%'
WHERE a.categorias IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM CategoriaAcademia ca
      WHERE ca.academia_id = a.id
        AND ca.categoria_id = c.id
  );
GO
----------------- FIM INSERTS CATEGORIAS DAS ACADEMIAS FICTÍCIAS -----------------

----------------- INSERTS FACILIDADES DAS ACADEMIAS FICTÍCIAS -----------------
INSERT INTO FacilidadeAcademia (academia_id, facilidade_id, statusFacilidadeAcademia)
SELECT
    a.id,
    f.id,
    'ATIVO'
FROM Academia a
CROSS APPLY STRING_SPLIT(a.facilidades, ',') s
INNER JOIN Facilidade f
    ON LOWER(LTRIM(RTRIM(s.value))) = LOWER(f.nome)
WHERE a.facilidades IS NOT NULL
  AND LTRIM(RTRIM(s.value)) <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM FacilidadeAcademia fa
      WHERE fa.academia_id = a.id
        AND fa.facilidade_id = f.id
  );
GO
----------------- FIM INSERTS FACILIDADES DAS ACADEMIAS FICTÍCIAS -----------------

----------------- SELECTS -----------------
SELECT * FROM Usuario
SELECT * FROM Gerente
SELECT * FROM Academia
SELECT * FROM FotoAcademia
SELECT * FROM Favorito
SELECT * FROM Avaliacao
SELECT * FROM ItemAvaliacao
SELECT * FROM ItemAvaliacaoAcademia
SELECT * FROM Categoria
SELECT * FROM CategoriaAcademia
SELECT * FROM Facilidade;
SELECT * FROM FacilidadeAcademia;
SELECT * FROM RecuperarSenha
----------------- FIM SELECTS -----------------