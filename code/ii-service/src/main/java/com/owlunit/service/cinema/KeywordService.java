package com.owlunit.service.cinema;

import com.owlunit.model.cinema.Keyword;
import com.owlunit.service.exception.NotFoundException;

import java.util.List;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface KeywordService {

    Keyword createKeyword(String name);

    Keyword loadOrCreateKeyword(String name);

    Keyword loadById(Long id) throws NotFoundException;

    Keyword loadByName(String name) throws NotFoundException;

    List<Keyword> listKeywords();

    Keyword update(Keyword keyword) throws NotFoundException;

}
