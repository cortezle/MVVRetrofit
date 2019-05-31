package com.petrlr14.mvvm.database.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.petrlr14.mvvm.database.RoomDB
import com.petrlr14.mvvm.database.entities.GitHubRepo
import com.petrlr14.mvvm.database.repositories.GitHubRepoRepository
import com.petrlr14.mvvm.service.retrofit.GithubService
import kotlinx.coroutines.launch

class GitHubRepoViewModel(private val app: Application) : AndroidViewModel(app) {

    private val repository: GitHubRepoRepository

    init {
        val repoDao=RoomDB.getInstance(app).repoDao()
        val githubService = GithubService.getGithubService()
        repository= GitHubRepoRepository(repoDao,githubService)
    }

    private suspend fun insert(repo:GitHubRepo)=repository.insert(repo)


    fun retrieveRepo(user:String)=viewModelScope.launch {

        /**
         * Primero se borra la tabla de repos
         **/
        this@GitHubRepoViewModel.nuke()

        /**
         * Obtiene el objeto Response de la consulta a la API
         **/
        val response=repository.retrieveReposAsync(user).await()

        /**
         * Evalua y decide dependiendo del estado de la respuesta obtenida
         **/
        if(response.isSuccessful) with(response){

            //Inserta toda la lista en la base de datos
            this.body()?.forEach {
                this@GitHubRepoViewModel.insert(it)
            }

        }else with(response){

            println(this.code())
            when(this.code()){
                //Aqui pueden evaluarse todos los codigos HTTP
                404->{
                    //Muestra un estado de error en caso no encuentre el usuario
                    Toast.makeText(app, "Usuario no encontrado", Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private suspend fun nuke()= repository.nuke()

}