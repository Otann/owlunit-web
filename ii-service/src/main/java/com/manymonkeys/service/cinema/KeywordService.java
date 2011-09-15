package com.manymonkeys.service.cinema;

import com.manymonkeys.model.cinema.Keyword;
import com.manymonkeys.service.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface KeywordService {

    Keyword createKeyword(String name);

    Keyword loadKeyword(UUID uuid);

    Keyword loadKeyword(String name);

    List<Keyword> listKeywords();

    Keyword update(Keyword keyword) throws NotFoundException;

}
