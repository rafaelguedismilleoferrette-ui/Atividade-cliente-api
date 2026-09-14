package com.example.demo;

import com.example.demo.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClienteControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void limparDados() {
        clienteRepository.deleteAll();
        jdbcTemplate.update("DELETE FROM cidade");
        jdbcTemplate.update("DELETE FROM estado");
    }

    @Test
    void deveSalvarListarBuscarAtualizarEDeletarCliente() throws Exception {
        ObjectNode cliente = novoCliente();
        ResponseEntity<String> cadastro = requisicao("POST", "/salvar-cliente", cliente);
        assertEquals(200, cadastro.getStatusCodeValue());

        JsonNode salvo = objectMapper.readTree(cadastro.getBody());
        long id = salvo.get("id").asLong();
        assertTrue(id > 0);
        cliente.set("id", salvo.get("id"));
        assertEquals(cliente, salvo);

        ResponseEntity<String> listagem = requisicao("GET", "/listar-clientes", null);
        assertEquals(200, listagem.getStatusCodeValue());
        JsonNode clientes = objectMapper.readTree(listagem.getBody());
        assertEquals(1, clientes.size());
        assertEquals(salvo, clientes.get(0));

        ResponseEntity<String> busca = requisicao("GET", "/buscar-cliente/" + id, null);
        assertEquals(200, busca.getStatusCodeValue());
        assertEquals(salvo, objectMapper.readTree(busca.getBody()));

        cliente.put("id", id + 1000);
        cliente.put("nome", "Cliente Atualizado");
        cliente.put("tipoPessoa", "PJ");
        cliente.put("cpfCnpj", "12345678000190");
        cliente.put("telefone", "4433333333");
        cliente.put("email", "atualizado@example.com");
        cliente.put("logradouro", "Rua Atualizada");
        cliente.put("numero", "10");
        cliente.put("bairro", "Bairro Atualizado");
        cliente.put("cep", "87000000");

        ResponseEntity<String> atualizacao = requisicao("PUT", "/atualizar-cliente/" + id, cliente);
        assertEquals(200, atualizacao.getStatusCodeValue());
        cliente.set("id", salvo.get("id"));
        assertEquals(cliente, objectMapper.readTree(atualizacao.getBody()));
        assertEquals(1, clienteRepository.count());

        ResponseEntity<String> buscaAtualizada = requisicao("GET", "/buscar-cliente/" + id, null);
        assertEquals(200, buscaAtualizada.getStatusCodeValue());
        assertEquals(cliente, objectMapper.readTree(buscaAtualizada.getBody()));

        ResponseEntity<String> exclusao = requisicao("DELETE", "/deletar-cliente/" + id, null);
        assertEquals(200, exclusao.getStatusCodeValue());
        assertTrue(exclusao.getBody() == null || exclusao.getBody().isEmpty());
        assertEquals(0, clienteRepository.count());

        ResponseEntity<String> listaVazia = requisicao("GET", "/listar-clientes", null);
        assertEquals(200, listaVazia.getStatusCodeValue());
        assertTrue(objectMapper.readTree(listaVazia.getBody()).isEmpty());
    }

    @Test
    void deveManterRelacionamentoComCidadeEEstado() throws Exception {
        jdbcTemplate.update("INSERT INTO estado (id, nome, uf, ibge) VALUES (1, 'Parana', 'PR', 41)");
        jdbcTemplate.update("INSERT INTO cidade (id, nome, uf, ibge) VALUES (1, 'Paranavai', 1, 4118402)");

        ObjectNode cliente = novoCliente();
        cliente.set("cidade", objectMapper.createObjectNode().put("id", 1));
        ResponseEntity<String> cadastro = requisicao("POST", "/salvar-cliente", cliente);
        assertEquals(200, cadastro.getStatusCodeValue());
        long id = objectMapper.readTree(cadastro.getBody()).get("id").asLong();

        ResponseEntity<String> busca = requisicao("GET", "/buscar-cliente/" + id, null);
        assertEquals(200, busca.getStatusCodeValue());
        JsonNode cidade = objectMapper.readTree(busca.getBody()).get("cidade");
        assertEquals(1, cidade.get("id").asLong());
        assertEquals("Paranavai", cidade.get("nome").asText());
        assertEquals(4118402, cidade.get("ibge").asInt());
        assertEquals("PR", cidade.get("estado").get("uf").asText());

        cliente.putNull("cidade");
        ResponseEntity<String> atualizacao = requisicao("PUT", "/atualizar-cliente/" + id, cliente);
        assertEquals(200, atualizacao.getStatusCodeValue());
        ResponseEntity<String> buscaAtualizada = requisicao("GET", "/buscar-cliente/" + id, null);
        assertEquals(200, buscaAtualizada.getStatusCodeValue());
        assertTrue(objectMapper.readTree(buscaAtualizada.getBody()).get("cidade").isNull());
    }

    private ObjectNode novoCliente() {
        return objectMapper.createObjectNode()
                .put("nome", "Cliente Teste")
                .put("tipoPessoa", "PF")
                .put("cpfCnpj", "11111111112")
                .put("telefone", "44999999999")
                .put("email", "cliente@example.com")
                .put("logradouro", "Avenida Teste")
                .put("numero", "S/N")
                .put("bairro", "Centro")
                .put("cep", "00000000")
                .putNull("cidade");
    }

    private ResponseEntity<String> requisicao(String metodo, String caminho, JsonNode corpo) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String conteudo = corpo == null ? null : objectMapper.writeValueAsString(corpo);
        HttpEntity<String> request = new HttpEntity<>(conteudo, headers);
        return restTemplate.exchange("/clientes" + caminho, HttpMethod.valueOf(metodo), request, String.class);
    }
}
