
# **Hackaton: Processamento de Vídeo**

## **Visão Geral do Projeto**

O projeto consiste em um sistema que processa vídeos e utiliza uma arquitetura de microsserviços, com integração via Kafka para comunicação assíncrona entre os componentes. O processamento envolve manipulação de vídeos (usando o FFmpeg) e o resultado é gerado como um arquivo ZIP contendo imagens extraídas do vídeo.

Os principais componentes são:

1. **API de Processamento de Vídeos**: Um microsserviço que recebe solicitações para processar vídeos.
2. **Kafka**: Usado para enviar e consumir mensagens relacionadas ao processamento de vídeos.
3. **PostgreSQL**: Utilizado como banco de dados para armazenar informações relacionadas.
4. **FFmpeg**: Utilizado para processar os vídeos, extraindo frames e gerando arquivos ZIP.
5. **Docker e Docker Compose**: Usados para gerenciar a infraestrutura e os serviços necessários para rodar o sistema, como Kafka, PostgreSQL e os microsserviços.

### **Funcionamento**

1. **Envio da Solicitação de Processamento**:
   Quando um cliente envia uma requisição para o microsserviço de processamento de vídeos (por exemplo, via uma API REST), um vídeo é identificado para processamento. O serviço envia uma **mensagem para o Kafka** com o ID do vídeo, solicitando que o vídeo seja processado. Isso é feito através de um **producer Kafka** que envia a mensagem para o **tópico Kafka** relevante (neste caso, o tópico `video-process-topic`).

   Aqui está um exemplo de log:

   ```
   Mensagem enviada ao Kafka para processar o vídeo: 2a064b6b-af7e-4830-a993-0211a8d20c72
   ```

2. **Processamento Assíncrono via Kafka**:
   O Kafka atua como um intermediário entre o serviço que solicita o processamento e o serviço que efetivamente processa o vídeo. Quando a mensagem é publicada no tópico `video-process-topic`, um **consumer Kafka** fica responsável por escutar esse tópico. No seu caso, você criou o consumer dentro da classe `KafkaVideoConsumer`, que é anotada com `@KafkaListener`.

   Esse **listener** consome a mensagem e chama o serviço de processamento de vídeo, passando o ID do vídeo. O processamento do vídeo ocorre de forma assíncrona, ou seja, o cliente que fez a solicitação original não precisa esperar o término do processamento.

   Exemplo de log quando a mensagem é recebida pelo consumer:

   ```
   Mensagem recebida para processar o vídeo: 2a064b6b-af7e-4830-a993-0211a8d20c72
   ```

3. **Processamento do Vídeo**:
   O serviço de processamento (`VideoProcessingService`) recebe o caminho do vídeo e usa o **FFmpeg** para processar o vídeo (extrair frames e salvar imagens). O processamento pode levar algum tempo, mas como o Kafka é baseado em mensagens assíncronas, esse tempo não afeta o fluxo principal da aplicação.

   Após o processamento, as imagens extraídas são salvas em um diretório, e um arquivo ZIP é gerado contendo essas imagens.

   Exemplo de log indicando que o processamento foi concluído:

   ```
   Processamento concluído e arquivo zip gerado: /videos/output/2a064b6b-af7e-4830-a993-0211a8d20c72.zip
   ```


### **Kafka no Processo**

Kafka é o coração da comunicação assíncrona entre os serviços no seu projeto. Aqui estão os pontos chave:

- **Producer**: O microsserviço de API ou qualquer outro serviço relevante atua como **producer** ao enviar uma mensagem para o Kafka quando o processamento de um vídeo é solicitado.

- **Topic**: As mensagens são enviadas para o **tópico** Kafka (`video-process-topic`). Cada mensagem contém informações sobre o vídeo que precisa ser processado.

- **Consumer**: O **consumer** (`KafkaVideoConsumer`) fica escutando o tópico. Assim que ele recebe uma mensagem, o processo de extração de frames do vídeo é iniciado.

Essa abordagem assíncrona é ideal para processamentos que podem ser demorados, como o processamento de vídeos, pois evita que o cliente fique aguardando por um longo período e garante que o serviço de processamento possa operar de maneira eficiente e escalável.

### **Pontos Adicionais**

- **Kafka facilita a escalabilidade**: Se o volume de vídeos crescer, é possível adicionar mais consumidores para processar os vídeos em paralelo.

- **Mensagens persistentes**: As mensagens enviadas ao Kafka são persistentes, o que significa que mesmo se o serviço de processamento de vídeo estiver temporariamente indisponível, as mensagens serão processadas assim que o serviço estiver novamente disponível.

Essa arquitetura garante que seu projeto seja flexível, escalável e eficiente no processamento de vídeos.

## Endpoints da Aplicação

### 1. **Upload de Vídeo**
   **URL:** `/videos/upload`  
   **Método:** `POST`  
   **Descrição:** Este endpoint permite que um vídeo seja enviado para processamento. O vídeo é salvo em um diretório local e uma mensagem é enviada para o Kafka para iniciar o processamento. Além do vídeo, é necessário fornecer o `username`, que identifica o usuário que fez o upload. O status inicial do vídeo será definido como `PENDING`, e durante o processamento, o status mudará para `PROCESSING`. Ao final, o status será atualizado para `COMPLETED` ou `FAILED` em caso de erro.

   **Parâmetros de Requisição:**
   - `video`: O arquivo de vídeo que será enviado. O arquivo deve ser do tipo `multipart/form-data`.
   - `username`: O nome de usuário associado ao upload do vídeo (do tipo `String`).

   **Exemplo de Body da Requisição:**
   Ao utilizar o Postman ou outra ferramenta, a requisição seria do tipo `form-data`:
   ```
   video: [Selecionar Arquivo: video.mp4]
   username: johndoe
   ```

   **Respostas:**
   - `200 OK`: Vídeo recebido e processamento iniciado: <videoId>
   - `400 Bad Request`: Houve um erro ao salvar o vídeo, o formato de arquivo é inválido, ou o `username` não foi fornecido.
   - `500 Internal Server Error`: Ocorreu um erro inesperado no processamento do vídeo.

### 2. **Consulta do Status do Vídeo**
   **URL:** `/videos/status/{videoId}`  
   **Método:** `GET`  
   **Descrição:** Este endpoint permite consultar o status de processamento de um vídeo específico. Ao enviar um vídeo para o endpoint de upload, ele é identificado por um `videoId` único. Este endpoint verifica o status atual do vídeo no banco de dados, retornando o status como `PENDING`, `PROCESSING`, `COMPLETED` ou `FAILED`.

   **Parâmetro de Caminho:**
   - `videoId`: O identificador único do vídeo cujo status será consultado.

   **Respostas:**
   - `200 OK`: Status do vídeo <videoId>: COMPLETED
   - `404 Not Found`: Erro ao buscar status do vídeo: Vídeo não encontrado <videoId>

### 2. **Lista todos vídeos**
   **URL:** `/videos`  
   **Método:** `GET`  
   **Descrição:**  Endpoint que retorna a lista completa dos vídeos independente do status.

   **Respostas:**
   - `200 OK`: Lista de registros de vídeo. Exemplo:
      ```
      [
         {
            "id": 1,
            "filename": "310dda18-a997-4140-93fc-77535fb13036",
            "status": "COMPLETED",
            "uploadTime": "2025-03-27T19:17:28.763438",
            "username": "johndoe"
         }
      ]
      ```
