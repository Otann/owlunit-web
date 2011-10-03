package com.manymonkeys.ex.json.controllers;

/**
 * @author Ilya Pimenov
 *         Owls Proproetary
 */
public interface OwlsFlatApi extends OwlsMovieApi, OwlsUserApi, OwlsRecommenderApi {

    String version();

    // mva-ha-ha !
}
