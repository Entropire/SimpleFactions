package com.entropire.simplefactions.objects;

import java.util.List;

public record Pageable
<T>(List<T> items, int currentPage, int maxPages, int itemsPerPage) {
    public Pageable(int currentPage, int itemsPerPage){
        this(null, currentPage, 0, itemsPerPage);
    }
}
