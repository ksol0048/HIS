package kroryi.his.mapper;

import java.util.List;

public interface GenericMapper<D,E> {

    D toDto (E e);
    E toEntity(D d);

    List<D> toDtoList(List<E> e);
    List<E> toEntityList(List<D> d);
}