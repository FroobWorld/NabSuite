package com.froobworld.nabsuite.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public final class ListPaginator {

    private ListPaginator() {}

    public static <T> List<T>[] paginate(List<T> list, int itemsPerPage) {
        return paginate(list, itemsPerPage, false);
    }

    public static <T> List<T>[] paginate(List<T> list, int itemsPerPage, boolean reverse) {
        if (itemsPerPage <= 0) {
            throw new IllegalArgumentException("The number of items per page must be positive");
        }
        @SuppressWarnings("unchecked") List<T>[] pages = (List<T>[]) Array.newInstance(List.class, numberOfPages(list, itemsPerPage));
        int currentPage = -1;
        for (int i = 0; i < list.size(); i++) {
            if (i % itemsPerPage == 0) {
                currentPage++;
                pages[currentPage] = new ArrayList<>();
            }
            int index = reverse ? (list.size() - i - 1) : i;
            pages[currentPage].add(list.get(index));
        }
        return pages;
    }

    public static int numberOfPages(int items, int itemsPerPage) {
        return (int) Math.ceil((double) items / itemsPerPage);
    }

    public static int numberOfPages(List<?> list, int itemsPerPage) {
        return numberOfPages(list.size(), itemsPerPage);
    }

}
