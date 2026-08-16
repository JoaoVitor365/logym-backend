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
----------------- FIM SELECTS -----------------
*/

CREATE TABLE Usuario (
    id INT IDENTITY(1,1) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nivelAcesso VARCHAR(10) NOT NULL,
    cep CHAR(8) NULL,
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

    PRIMARY KEY (id),
    FOREIGN KEY (academia_id) REFERENCES Academia(id),

    CONSTRAINT CK_FotoAcademia_Status
    CHECK (statusFoto IN ('ATIVO', 'INATIVO'))
);
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

----------------- INSERTS DE USUÁRIOS -----------------

-- USUÁRIO nivelAcesso=ADMIN
INSERT Usuario (nome, username, password, nivelAcesso, cep, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('Admin', 'admin@logym.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'ADMIN', NULL, NULL, GETDATE(), NULL, 'ATIVO');
--                                   123123

-- USUÁRIO nivelAcesso=USER
INSERT Usuario (nome, username, password, nivelAcesso, cep, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('João Vitor', 'joaovitor@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'USER', '06401050', NULL, GETDATE(), NULL, 'ATIVO');
--                                            123123

-- USUÁRIO nivelAcesso=MANAGER
INSERT Usuario (nome, username, password, nivelAcesso, cep, foto, dataCadastro, dataAtualizacao, statusUsuario)
VALUES ('João Pedro', 'joaopedro@email.com', '$2a$10$anXp8SeNMzeIpQKKwMt2Y.qxs3uTkvwlT8ypuJ3BgA/EWAIqrgE/.', 'MANAGER', NULL, NULL, GETDATE(), NULL, 'ATIVO');
--                                            123123

-- GERENTE
INSERT Gerente (nome, cpf, telefone, dataNascimento, usuario_id, dataCadastro, statusGerente)
VALUES ('João Pedro', '99202457042', '(11) 99999-8888', '2000-04-04', 3, GETDATE(), 'ATIVO');
--

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
----------------- FIM SELECTS -----------------
*/

----------------- INSERTS ACADEMIAS FICTÍCIAS -----------------
INSERT INTO Academia
(
    nome, cnpj, descricao, cep, endereco, numero, complemento,
    bairro, cidade, estado, telefone, celular, email,
    categorias, facilidades, nota, gerente_id, dataCadastro, statusAcademia
)
VALUES
(
    'Smart Fit Barueri Centro',
    '90100000000156',
    'Academia da rede Smart Fit localizada no Centro de Barueri, com estrutura para musculação e treinos funcionais.',
    '06401050',
    'Avenida Vinte e Seis de Março',
    701,
    NULL,
    'Centro',
    'Barueri',
    'SP',
    '(11) 4002-1000',
    '(11) 99999-1000',
    'smartfit.baruericentro@logym.com',
    'Musculação, Funcional, Personal Trainer',
    'Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Estrada das Pitas',
    '90100001000109',
    'Academia da rede Smart Fit localizada na região do Parque Viana, em Barueri.',
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
    'Musculação, Funcional, Personal Trainer',
    'Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Parque Shopping Barueri',
    '90100002000145',
    'Academia da rede Smart Fit localizada no Parque Shopping Barueri.',
    '06440180',
    'Rua General de Divisão de Pedro Rodrigues da Silva',
    400,
    'Parque Shopping Barueri',
    'Nova Aldeinha',
    'Barueri',
    'SP',
    '(11) 4002-1002',
    '(11) 99999-1002',
    'smartfit.parqueshoppingbarueri@logym.com',
    'Musculação, Funcional, Personal Trainer',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Carrefour Hiper Tamboré',
    '90100003000190',
    'Academia da rede Smart Fit localizada na região de Alphaville/Tamboré.',
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
    'Musculação, Funcional, Personal Trainer',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Sodimac Alphaville',
    '90100004000134',
    'Academia da rede Smart Fit localizada no estacionamento do Sodimac Alphaville.',
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
    'Musculação, Funcional, Personal Trainer',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Smart Fit Shopping Flamingo Alphaville',
    '90100005000189',
    'Academia da rede Smart Fit localizada no Shopping Flamingo Alphaville.',
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
    'Musculação, Funcional, Personal Trainer',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Barueri',
    '90100006000123',
    'Academia da rede Bluefit localizada em Bethaville, Barueri.',
    '06404326',
    'Avenida Trindade',
    344,
    'Loja 2023',
    'Bethaville I',
    'Barueri',
    'SP',
    '(11) 4002-1006',
    '(11) 99999-1006',
    'bluefit.barueri@logym.com',
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
),
(
    'Bluefit Alphaville',
    '90100007000178',
    'Academia da rede Bluefit localizada em Alphaville, Barueri.',
    '06454010',
    'Alameda Amazonas',
    388,
    NULL,
    'Alphaville Centro Industrial',
    'Barueri',
    'SP',
    '(11) 4002-1007',
    '(11) 99999-1007',
    'bluefit.alphaville@logym.com',
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
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
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
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
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
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
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
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
    'Musculação, Funcional, Lutas, Spinning',
    'Estacionamento, Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
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
    'Musculação, Funcional, Personal Trainer',
    'Ar-condicionado, Vestiário, Chuveiro, Wi-Fi, Armários',
    NULL,
    1,
    GETDATE(),
    'ATIVO'
);

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
----------------- FIM SELECTS -----------------


----------------- VERIFICAÇÕES RÁPIDAS -----------------
-- Deve retornar 0 linhas: academias fictícias sem vínculos de categoria.
SELECT a.id, a.nome
FROM Academia a
WHERE a.categorias IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM CategoriaAcademia ca WHERE ca.academia_id = a.id
  );

-- Deve retornar 0 linhas: academias fictícias sem vínculos de facilidade.
SELECT a.id, a.nome
FROM Academia a
WHERE a.facilidades IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM FacilidadeAcademia fa WHERE fa.academia_id = a.id
  );

-- Conferência dos novos campos da Academia.
SELECT id, nome, latitude, longitude, statusAcademia, statusAnteriorBloqueioGerente
FROM Academia
ORDER BY id;
----------------- FIM VERIFICAÇÕES RÁPIDAS -----------------

