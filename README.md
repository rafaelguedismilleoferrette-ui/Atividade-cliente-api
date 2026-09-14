# Cadastro de clientes

Projeto Maven com Java 8, Spring Boot 2.7.18 e `javax.persistence`, organizado em controller, service e repository.

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação atende em `http://localhost:8080`.

## Endpoints

| Método | Rota |
| --- | --- |
| POST | `/clientes/salvar-cliente` |
| GET | `/clientes/listar-clientes` |
| GET | `/clientes/buscar-cliente/{id}` |
| PUT | `/clientes/atualizar-cliente/{id}` |
| DELETE | `/clientes/deletar-cliente/{id}` |

Corpo para salvar ou atualizar:

```json
{
  "nome": "Cliente Teste",
  "tipoPessoa": "PF",
  "cpfCnpj": "11111111112",
  "telefone": "44999999999",
  "email": "cliente@example.com",
  "logradouro": "Avenida Teste",
  "numero": "S/N",
  "bairro": "Centro",
  "cep": "00000000",
  "cidade": null
}
```
