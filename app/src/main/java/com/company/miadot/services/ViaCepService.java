package com.company.miadot.services;

import com.company.miadot.model.CepResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ViaCepService {
    @GET("{cep}/json/")
    Call<CepResponse> buscarCep(@Path("cep") String cep);
}
