package com.example.medicatrack.repo.persist.impl;

import android.content.Context;

import com.example.medicatrack.model.enums.Color;
import com.example.medicatrack.model.enums.Forma;
import com.example.medicatrack.model.enums.Unidad;
import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.enums.Frecuencia;
import com.example.medicatrack.repo.persist.database.Database;
import com.example.medicatrack.repo.persist.database.daos.MedicamentoDao;
import com.example.medicatrack.repo.persist.entities.MedicamentoEntity;
import com.example.medicatrack.repo.persist.interfaces.CallbacksDataSource;
import com.example.medicatrack.repo.persist.interfaces.MedicamentoDataSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class MedicamentoRoomDataSource implements MedicamentoDataSource
{
    private static MedicamentoDao medicamentoDao;
    private static MedicamentoRoomDataSource INSTANCE = null;

    private MedicamentoRoomDataSource(final Context context)
    {
        Database bd = Database.getInstance(context);
        medicamentoDao = bd.medicamentoDao();
    }

    public synchronized static MedicamentoRoomDataSource getInstance(final Context context)
    {
        if(INSTANCE == null) INSTANCE = new MedicamentoRoomDataSource(context);
        return INSTANCE;
    }

    @Override
    public void insert(Medicamento medicamento, CallbacksDataSource.InsertCallback callback)
    {
        if(medicamento == null) callback.onInsert(false);
        else
        {
            MedicamentoEntity entity = new MedicamentoEntity();
            entity.setColor(medicamento.getColor().name());
            entity.setConcentracion(medicamento.getConcentracion());
            entity.setUnidad(medicamento.getUnidad().name());
            entity.setForma(medicamento.getForma().name());
            entity.setNombre(medicamento.getNombre());
            entity.setFrecuencia(medicamento.getFrecuencia().name());
            entity.setFechaInicio(medicamento.getFechaInicio().toInstant().getEpochSecond());
            entity.setDias(medicamento.getDias());
            entity.setHora(medicamento.getHora().toInstant().getEpochSecond());
            entity.setDescripcion(medicamento.getDescripcion());
            try
            {
                medicamentoDao.insert(entity);
                callback.onInsert(true);

            }catch (Exception e)
            {
                callback.onInsert(false);
            }
        }
    }

    @Override
    public void update(Medicamento medicamento, CallbacksDataSource.UpdateCallback callback)
    {
        //To do
    }

    @Override
    public void getById(int id, CallbacksDataSource.GetByIdCallback<Medicamento> callback)
    {
        try
        {
            MedicamentoEntity entity = medicamentoDao.getById(id);
            Medicamento medicamento = new Medicamento(entity.getId());
            medicamento.setColor(Color.valueOf(entity.getColor()));
            medicamento.setNombre(entity.getNombre());
            medicamento.setFrecuencia(Frecuencia.valueOf(entity.getFrecuencia()));
            medicamento.setForma(Forma.valueOf(entity.getForma()));
            medicamento.setConcentracion(entity.getConcentracion());
            medicamento.setUnidad(Unidad.valueOf(entity.getUnidad()));
            medicamento.setDias(entity.getDias());
            medicamento.setFechaInicio(ZonedDateTime.ofInstant(Instant.ofEpochSecond(entity.getFechaInicio()),ZoneId.of("America/Argentina/Buenos_Aires")));
            medicamento.setHora(ZonedDateTime.ofInstant(Instant.ofEpochSecond(entity.getHora()),ZoneId.of("America/Argentina/Buenos_Aires")));
            medicamento.setDescripcion(entity.getDescripcion());
            callback.onGetById(true,medicamento);
        } catch(Exception e)
        {
            callback.onGetById(false,null);
        }
    }

    @Override
    public void getAll(CallbacksDataSource.GetAllCallback<Medicamento> callback)
    {
        //To do
    }
}
