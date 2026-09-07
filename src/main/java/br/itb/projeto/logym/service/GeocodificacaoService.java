package br.itb.projeto.logym.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.itb.projeto.logym.dto.CoordenadasDTO;
import br.itb.projeto.logym.dto.EnderecoGeocodificacaoDTO;
import br.itb.projeto.logym.exception.GeocodificacaoException;

@Service
public class GeocodificacaoService {

    private static final BigDecimal LATITUDE_MINIMA = new BigDecimal("-90");
    private static final BigDecimal LATITUDE_MAXIMA = new BigDecimal("90");
    private static final BigDecimal LONGITUDE_MINIMA = new BigDecimal("-180");
    private static final BigDecimal LONGITUDE_MAXIMA = new BigDecimal("180");

    private final RestClient restClient;
    private final String apiKey;
    private final ConcurrentMap<String, CoordenadasDTO> cache = new ConcurrentHashMap<>();
    private final Object geocodificacaoLock = new Object();

    public GeocodificacaoService(
            @Value("${google.maps.geocoding.url}") String baseUrl,
            @Value("${google.maps.geocoding.api-key}") String apiKey,
            @Value("${google.maps.geocoding.timeout-millis}") int timeoutMillis) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.apiKey = apiKey;
    }

    public CoordenadasDTO geocodificar(EnderecoGeocodificacaoDTO endereco) {
        String consulta = montarConsulta(endereco);
        String chaveDoCache = normalizarChaveDoCache(consulta);

        CoordenadasDTO coordenadasEmCache = cache.get(chaveDoCache);
        if (coordenadasEmCache != null) {
            return coordenadasEmCache;
        }

        synchronized (geocodificacaoLock) {
            coordenadasEmCache = cache.get(chaveDoCache);
            if (coordenadasEmCache != null) {
                return coordenadasEmCache;
            }

            CoordenadasDTO coordenadas = consultarGoogle(consulta);
            cache.put(chaveDoCache, coordenadas);
            return coordenadas;
        }
    }

    private CoordenadasDTO consultarGoogle(String consulta) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                throw new GeocodificacaoException("Chave da API de geocodificacao nao configurada.");
            }

            GoogleGeocodificacaoResposta resposta = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment(consulta)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(GoogleGeocodificacaoResposta.class);

            if (resposta == null || resposta.results() == null || resposta.results().length == 0) {
                throw new GeocodificacaoException("Endereco nao encontrado para geocodificacao.");
            }

            boolean coordenadasAusentes = true;
            for (GoogleGeocodificacaoResultado resultado : resposta.results()) {
                if (resultado == null || resultado.location() == null
                        || resultado.location().latitude() == null || resultado.location().longitude() == null) {
                    continue;
                }

                coordenadasAusentes = false;
                CoordenadasDTO coordenadas = new CoordenadasDTO(
                        resultado.location().latitude(),
                        resultado.location().longitude());

                if (coordenadasSaoValidas(coordenadas)) {
                    return coordenadas;
                }
            }

            if (coordenadasAusentes) {
                throw new GeocodificacaoException("Coordenadas ausentes na resposta de geocodificacao.");
            }

            throw new GeocodificacaoException("Coordenadas invalidas retornadas pela geocodificacao.");
        } catch (GeocodificacaoException e) {
            throw e;
        } catch (RestClientException | IllegalArgumentException e) {
            throw new GeocodificacaoException("Nao foi possivel geocodificar o endereco informado.", e);
        }
    }

    private String montarConsulta(EnderecoGeocodificacaoDTO endereco) {
        if (endereco == null) {
            throw new GeocodificacaoException("Endereco nao informado para geocodificacao.");
        }

        String logradouro = normalizarTexto(endereco.endereco());
        String numero = normalizarNumero(endereco.numero());
        String bairro = normalizarTexto(endereco.bairro());
        String cidade = normalizarTexto(endereco.cidade());
        String estado = normalizarTexto(endereco.estado());
        String cep = normalizarCep(endereco.cep());

        List<String> componentes = new ArrayList<>();
        adicionarSePreenchido(componentes, logradouro);
        adicionarSePreenchido(componentes, numero);
        adicionarSePreenchido(componentes, bairro);
        adicionarSePreenchido(componentes, cidade);
        adicionarSePreenchido(componentes, estado);

        if (cep != null) {
            componentes.add("CEP " + cep);
        }

        adicionarSePreenchido(componentes, "Brasil");
        String consultaCompleta = String.join(", ", componentes);

        if (logradouro == null || cidade == null || estado == null) {
            throw new GeocodificacaoException("Endereco insuficiente para geocodificacao.");
        }

        return consultaCompleta;
    }

    private String normalizarNumero(BigDecimal numero) {
        if (numero == null) {
            return null;
        }

        return numero.stripTrailingZeros().toPlainString();
    }

    private String normalizarChaveDoCache(String consulta) {
        return consulta.toLowerCase(Locale.ROOT);
    }

    private void validarCoordenadas(CoordenadasDTO coordenadas) {
        BigDecimal latitude = coordenadas.latitude();
        BigDecimal longitude = coordenadas.longitude();

        if (latitude == null || longitude == null
                || latitude.compareTo(LATITUDE_MINIMA) < 0
                || latitude.compareTo(LATITUDE_MAXIMA) > 0
                || longitude.compareTo(LONGITUDE_MINIMA) < 0
                || longitude.compareTo(LONGITUDE_MAXIMA) > 0) {
            throw new GeocodificacaoException("Coordenadas invalidas retornadas pela geocodificacao.");
        }
    }

    private boolean coordenadasSaoValidas(CoordenadasDTO coordenadas) {
        try {
            validarCoordenadas(coordenadas);
            return true;
        } catch (GeocodificacaoException e) {
            return false;
        }
    }

    private void adicionarSePreenchido(List<String> componentes, String valor) {
        if (valor != null) {
            componentes.add(valor);
        }
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }

        String valorNormalizado = valor.trim().replaceAll("\\s+", " ");
        return valorNormalizado.isBlank() ? null : valorNormalizado;
    }

    private String normalizarCep(String cep) {
        if (cep == null) {
            return null;
        }

        String cepNormalizado = cep.replaceAll("\\D", "");
        return cepNormalizado.isBlank() ? null : cepNormalizado;
    }

    private record GoogleGeocodificacaoResposta(GoogleGeocodificacaoResultado[] results) {
    }

    private record GoogleGeocodificacaoResultado(GoogleLocalizacao location) {
    }

    private record GoogleLocalizacao(BigDecimal latitude, BigDecimal longitude) {
    }
}
