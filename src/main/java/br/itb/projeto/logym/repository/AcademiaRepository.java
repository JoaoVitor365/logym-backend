package br.itb.projeto.logym.repository;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.itb.projeto.logym.model.entity.Academia;

public interface AcademiaRepository extends JpaRepository<Academia, Long> {

    List<Academia> findByStatusAcademia(String statusAcademia);

    List<Academia> findByGerenteIdAndStatusAcademia(Long gerenteId, String statusAcademia);

    List<Academia> findByGerenteId(Long gerenteId);

    @Query("""
            select a
            from Academia a
            where a.statusAcademia = 'ATIVO'
              and (
                    :search is null
                    or lower(a.nome) like lower(concat('%', :search, '%'))
                    or lower(a.cidade) like lower(concat('%', :search, '%'))
                    or lower(a.endereco) like lower(concat('%', :search, '%'))
              )
              and (
                    :categoriaIds is null
                    or exists (
                        select ca.id
                        from CategoriaAcademia ca
                        where ca.academia = a
                          and ca.statusCategoriaAcademia = 'ATIVO'
                          and ca.categoria.statusCategoria = 'ATIVO'
                          and ca.categoria.id in :categoriaIds
                    )
              )
              and (
                    :facilidadeIds is null
                    or exists (
                        select fa.id
                        from FacilidadeAcademia fa
                        where fa.academia = a
                          and fa.statusFacilidadeAcademia = 'ATIVO'
                          and fa.facilidade.statusFacilidade = 'ATIVO'
                          and fa.facilidade.id in :facilidadeIds
                    )
              )
            order by
                case
                    when :latitudeUsuario is not null
                     and :longitudeUsuario is not null
                     and a.latitude between -90 and 90
                     and a.longitude between -180 and 180
                     and (2.0 * 6371.0 * atan2(
                            sqrt(
                                sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                    * sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                + cos(radians(:latitudeUsuario)) * cos(radians(a.latitude))
                                    * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                                    * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                            ),
                            sqrt(1.0 - (
                                sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                    * sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                + cos(radians(:latitudeUsuario)) * cos(radians(a.latitude))
                                    * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                                    * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                            ))
                        )) <= 5.0
                    then 0
                    else 1
                end asc,
                case
                    when :latitudeUsuario is not null
                     and :longitudeUsuario is not null
                     and a.latitude between -90 and 90
                     and a.longitude between -180 and 180
                     and (2.0 * 6371.0 * atan2(
                            sqrt(
                                sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                    * sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                + cos(radians(:latitudeUsuario)) * cos(radians(a.latitude))
                                    * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                                    * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                            ),
                            sqrt(1.0 - (
                                sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                    * sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                + cos(radians(:latitudeUsuario)) * cos(radians(a.latitude))
                                    * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                                    * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                            ))
                        )) <= 5.0
                    then (2.0 * 6371.0 * atan2(
                        sqrt(
                            sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                * sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                            + cos(radians(:latitudeUsuario)) * cos(radians(a.latitude))
                                * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                                * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                        ),
                        sqrt(1.0 - (
                            sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                                * sin(radians((a.latitude - :latitudeUsuario) / 2.0))
                            + cos(radians(:latitudeUsuario)) * cos(radians(a.latitude))
                                * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                                * sin(radians((a.longitude - :longitudeUsuario) / 2.0))
                        ))
                    ))
                    else null
                end asc,
                a.id asc
            """)
    Page<Academia> findAtivasParaHome(
            @Param("search") String search,
            @Param("categoriaIds") List<Long> categoriaIds,
            @Param("facilidadeIds") List<Long> facilidadeIds,
            @Param("latitudeUsuario") BigDecimal latitudeUsuario,
            @Param("longitudeUsuario") BigDecimal longitudeUsuario,
            Pageable pageable);
}
