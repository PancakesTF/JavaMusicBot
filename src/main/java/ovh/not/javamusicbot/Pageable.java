/*
 * Copyright (c) 2003-2006, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ovh.not.javamusicbot;

import java.util.List;

/**
 * Helper class that implements paging over a collection.
 *
 * @author Simon Brown
 */
public class Pageable<T> {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int PAGE_WINDOW = 10;

    private List<T> list;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private int page, startingIndex, endingIndex, maxPages;

    public Pageable(List<T> list) {
        this.list = list;
        this.page = 1;
        this.maxPages = 1;
        calculatePages();
    }

    private void calculatePages() {
        if (pageSize > 0) {
            if (list.size() % pageSize == 0) {
                maxPages = list.size() / pageSize;
            } else {
                maxPages = (list.size() / pageSize) + 1;
            }
        }
    }

    public List<T> getList() {
        return this.list;
    }

    public List<T> getListForPage() {
        return list.subList(startingIndex, endingIndex);
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        calculatePages();
    }

    public int getPage() {
        return this.page;
    }

    public void setPage(int p) {
        if (p >= maxPages) {
            this.page = maxPages;
        } else if (p <= 1) {
            this.page = 1;
        } else {
            this.page = p;
        }
        startingIndex = pageSize * (page - 1);
        if (startingIndex < 0) {
            startingIndex = 0;
        }
        endingIndex = startingIndex + pageSize;
        if (endingIndex > list.size()) {
            endingIndex = list.size();
        }
    }

    public int getMaxPages() {
        return this.maxPages;
    }

    public int getPreviousPage() {
        if (page > 1) {
            return page - 1;
        } else {
            return 0;
        }
    }

    public int getNextPage() {
        if (page < maxPages) {
            return page + 1;
        } else {
            return 0;
        }
    }

    public int getMinPageRange() {
        if (getPage() > PAGE_WINDOW) {
            return getPage() - PAGE_WINDOW;
        } else {
            return 1;
        }
    }

    public int getMaxPageRange() {
        if (getPage() < (getMaxPages() - PAGE_WINDOW)) {
            return getPage() + PAGE_WINDOW;
        } else {
            return getMaxPages();
        }
    }
}
