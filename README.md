 <h1>Serviço de Gestão de Arquivos</h1>

<h2>Visão Geral</h2>
O Serviço de Gestão de Arquivos é uma aplicação Spring Boot que fornece funcionalidades básicas para upload, armazenamento e download de arquivos. Este serviço serve como base para um sistema mais robusto de gerenciamento de arquivos, podendo ser estendido com recursos adicionais conforme necessário.

<h2>Funcionalidades Principais</h2>

- Upload de Arquivos: Permite o envio de arquivos para o servidor
- Listagem de Arquivos: Exibe todos os arquivos armazenados
- Download de Arquivos: Permite o download dos arquivos armazenados
- Gerenciamento de Armazenamento: Inicialização e limpeza do diretório de armazenamento

<h2>Configuração Técnica</h2>
<h3>Tecnologias Utilizadas</h3>

- Spring Boot: Última versão (compatível com Spring 5+)
- Thymeleaf: Para renderização de templates HTML
- Spring MVC: Para manipulação de requisições HTTP
- Spring Configuration Properties: Para configuração personalizada

<h3>Estrutura do Projeto</h3>

com.azvtech.file_management
├── controller
│   └── FileUploadController.java        # Controlador para upload/download
├── storage
│   ├── StorageService.java              # Interface do serviço
│   ├── FileSystemStorageService.java    # Implementação do serviço
│   ├── StorageProperties.java           # Configurações de armazenamento
│   ├── StorageException.java            # Exceções personalizadas
│   └── StorageFileNotFoundException.java
└── FileManagementApplication.java       # Classe principal

<h3>Configurações</h3>

- Tamanho máximo de arquivo: 128KB (configurável em application.properties)
- Diretório de armazenamento padrão: "upload-dir" (configurável via StorageProperties)

Endpoints da API
Método	Endpoint	Descrição
GET	/	Exibe formulário de upload e lista de arquivos
POST	/	Processa upload de arquivo
GET	/files/{nome}	Faz download do arquivo especificado
Como Executar
Clone o repositório

Configure o diretório de armazenamento em StorageProperties (opcional)

Execute a classe FileManagementApplication

Acesse http://localhost:8080 no navegador

Fluxo de Operação
Inicialização:

O diretório de armazenamento é limpo e recriado

O servidor Spring Boot é iniciado

Upload:

Usuário seleciona arquivo via formulário HTML

Arquivo é validado (tamanho, não vazio)

Arquivo é armazenado no diretório configurado

Download:

Usuário clica em link na lista de arquivos

Servidor verifica existência do arquivo

Arquivo é enviado como resposta com headers apropriados

Próximas Melhorias (Roadmap)
Autenticação e Autorização: Proteger endpoints com Spring Security

Metadados de Arquivos: Armazenar informações adicionais sobre os arquivos

Limites Customizáveis: Permitir configuração por usuário/arquivo

Tipos de Arquivo: Validação de tipos MIME permitidos

Armazenamento em Nuvem: Integração com AWS S3, Google Cloud Storage, etc.

Compressão: Opção para compactar arquivos antes do armazenamento

Criptografia: Armazenamento seguro de arquivos sensíveis

Versionamento: Controle de versões de arquivos

API RESTful: Endpoints JSON para integração com frontends modernos

Considerações de Segurança
Atualmente o serviço inclui algumas proteções básicas:

Verificação de caminhos relativos (prevenção contra directory traversal)

Limitação de tamanho de arquivos

Validação de arquivos vazios

Recomenda-se implementar medidas adicionais para ambientes de produção:

Autenticação de usuários

Logging de operações

Quotas de armazenamento

Varredura antivírus para uploads

Configuração Personalizada
Para alterar o diretório de armazenamento, adicione no application.properties:

properties
storage.location=/caminho/para/seu/diretorio
Para alterar os limites de tamanho:

properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
Dependências Necessárias
O projeto requer as seguintes dependências Spring Boot:

spring-boot-starter-web

spring-boot-starter-thymeleaf

spring-boot-configuration-processor

Esta documentação serve como ponto de partida para o desenvolvimento de um serviço completo de gerenciamento de arquivos. Cada componente pode ser estendido para atender a requisitos específicos de negócio ou técnicos.
