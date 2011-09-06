package com.manymonkeys.service.cinema;

import com.manymonkeys.model.cinema.Keyword;

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

    Keyword updateName(Keyword keyword, String name);

    Boolean isKeyword(Keyword keyword);
}
