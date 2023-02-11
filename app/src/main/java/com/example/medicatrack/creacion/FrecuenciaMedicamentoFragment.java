package com.example.medicatrack.creacion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.medicatrack.R;
import com.example.medicatrack.creacion.utilities.Utilities;
import com.example.medicatrack.creacion.viewmodels.CreacionViewModel;
import com.example.medicatrack.databinding.FechaHoraBinding;
import com.example.medicatrack.databinding.FragmentFrecuenciaMedicamentoBinding;
import com.example.medicatrack.model.Medicamento;
import com.example.medicatrack.model.enums.Color;
import com.example.medicatrack.model.enums.Forma;
import com.example.medicatrack.model.enums.Frecuencia;
import com.example.medicatrack.model.enums.Unidad;
import com.example.medicatrack.repo.persist.impl.MedicamentoRoomDataSource;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FrecuenciaMedicamentoFragment extends Fragment {

    private FragmentFrecuenciaMedicamentoBinding binding;
    private CreacionViewModel viewModel;

    private ZonedDateTime fechaSeleccionada;
    private ZonedDateTime horaSeleccionada;
    private int frecSeleccionada;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(CreacionViewModel.class);
        // Dado que, de manera muy rara, cuando se llega a este fragmento, el titulo vuelve a "Medicamento", lo seteo nuevamente al valor actual del nonbre
        viewModel.setNombreMed(viewModel.getNombreMed().getValue());
        frecSeleccionada = 0; // Frecuencia por defecto
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFrecuenciaMedicamentoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar dropdown frecuencia
        String[] listaFrecuencia = getResources().getStringArray(R.array.frecuencias);
        binding.dropFrecuencia.setAdapter(new ArrayAdapter<>(getContext(), R.layout.lista_dropdown, R.id.txtLista, listaFrecuencia));

        // Contenedor de fecha y hora de para la opcion Todos los dias
        setearFechaYHora(binding.contFechaHora);

        // Setear informacion al contenedor de intervalos regulares
        List<String> intervalos = new ArrayList<>();
        intervalos.add("1 día");
        for (int i = 2; i < 100; i++) {
            intervalos.add(i + " días");
        }
        binding.contIntRegulares.dropFrecuencia.setAdapter(new ArrayAdapter<>(getContext(), R.layout.lista_dropdown, R.id.txtLista, intervalos));
        setearFechaYHora(binding.contIntRegulares.contFechaHora2);

        // Manejo de chips del contenedor de dias especificos
        String[] dias = getResources().getStringArray(R.array.dias);
        for (int i = 0; i < 7; i++) {
            Chip chip = new Chip(this.getContext());
            chip.setText(dias[i]);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> { // Cuando se presione alguno, se va el label del error
                binding.contDias.errDias.setVisibility(View.GONE);
            });
            binding.contDias.diasChipGroup.addView(chip);
        }
        setearFechaYHora(binding.contDias.contFechaHora3);


        // Dependiendo de la opcion elegida del tipo de frecuencia, se habilitan las opciones abajo
        binding.dropFrecuencia.setOnItemClickListener((parent, vista, position, id) -> {
            switch (position) {
                case 0: { // Todos los dias
                    binding.contFechaHora.contFechaHora.setVisibility(View.VISIBLE);
                    if (fechaSeleccionada != null) {
                        binding.contFechaHora.editFecha.setText(fechaSeleccionada.getDayOfMonth() + "/" + fechaSeleccionada.getMonthValue() + "/" + fechaSeleccionada.getYear());
                        binding.contFechaHora.errFecha.setVisibility(View.INVISIBLE);
                    }
                    if (horaSeleccionada != null) {
                        binding.contFechaHora.editHora.setText(Utilities.formatearHora(horaSeleccionada.getHour(), horaSeleccionada.getMinute()));
                        binding.contFechaHora.errHora.setVisibility(View.INVISIBLE);
                    }
                    binding.contIntRegulares.contenedor.setVisibility(View.GONE);
                    binding.contDias.contDias.setVisibility(View.GONE);
                    frecSeleccionada = 0;
                }
                break;
                case 1: { // Intervalos regulares
                    binding.contIntRegulares.contenedor.setVisibility(View.VISIBLE);
                    if (fechaSeleccionada != null) {
                        binding.contIntRegulares.contFechaHora2.editFecha.setText(fechaSeleccionada.getDayOfMonth() + "/" + fechaSeleccionada.getMonthValue() + "/" + fechaSeleccionada.getYear());
                        binding.contIntRegulares.contFechaHora2.errFecha.setVisibility(View.INVISIBLE);
                    }
                    if (horaSeleccionada != null) {
                        binding.contIntRegulares.contFechaHora2.editHora.setText(Utilities.formatearHora(horaSeleccionada.getHour(), horaSeleccionada.getMinute()));
                        binding.contIntRegulares.contFechaHora2.errHora.setVisibility(View.INVISIBLE);
                    }
                    binding.contDias.contDias.setVisibility(View.GONE);
                    binding.contFechaHora.contFechaHora.setVisibility(View.GONE);
                    frecSeleccionada = 1;
                }
                break;
                case 2: { // Dias de la semana especificos
                    binding.contDias.contDias.setVisibility(View.VISIBLE);
                    if (fechaSeleccionada != null) {
                        binding.contDias.contFechaHora3.editFecha.setText(fechaSeleccionada.getDayOfMonth() + "/" + fechaSeleccionada.getMonthValue() + "/" + fechaSeleccionada.getYear());
                        binding.contDias.contFechaHora3.errFecha.setVisibility(View.INVISIBLE);
                    }
                    if (horaSeleccionada != null) {
                        binding.contDias.contFechaHora3.editHora.setText(Utilities.formatearHora(horaSeleccionada.getHour(), horaSeleccionada.getMinute()));
                        binding.contDias.contFechaHora3.errHora.setVisibility(View.INVISIBLE);
                    }
                    binding.contIntRegulares.contenedor.setVisibility(View.GONE);
                    binding.contFechaHora.contFechaHora.setVisibility(View.GONE);
                    frecSeleccionada = 2;
                }
                break;
                case 3: { // Segun se necesite
                    binding.contIntRegulares.contenedor.setVisibility(View.GONE);
                    binding.contDias.contDias.setVisibility(View.GONE);
                    binding.contFechaHora.contFechaHora.setVisibility(View.GONE);
                    frecSeleccionada = 3;
                }
                break;
            }
        });


        // Boton LISTO
        binding.fab.setOnClickListener(vista -> {
            if (verificarDatos()) {
                Medicamento med = new Medicamento(1);
                med.setNombre(viewModel.getNombreMed().getValue());
                med.setColor(Color.valueOf(viewModel.getColor().getValue().toUpperCase()));
                med.setForma(Forma.valueOf(viewModel.getForma().getValue().toUpperCase()));
                med.setConcentracion(viewModel.getConcentracion().getValue());
                med.setUnidad(viewModel.getUnidad().getValue().equals("%") ? Unidad.PORCENTAJE : Unidad.valueOf(viewModel.getUnidad().getValue().toUpperCase()));
                med.setFrecuencia(Frecuencia.values()[frecSeleccionada]);
                switch (frecSeleccionada) {
                    case 0: // Todos los dias
                    case 3: { // Segun se necesite
                        med.setDias("");
                    }
                    break;
                    case 1: { // Intervalos regulares
                        med.setDias(String.valueOf(binding.contIntRegulares.dropFrecuencia.getText().charAt(0)));
                    }
                    break;
                    case 2: { // Dias de la semana especificos
                        String diasEspecif = "";
                        for (Integer num: binding.contDias.diasChipGroup.getCheckedChipIds()) { // [1, 2, 4] siendo Lunes - 1
                            diasEspecif = diasEspecif+num;
                        }
                        med.setDias(diasEspecif);
                    }
                    break;
                }
                med.setFechaInicio(fechaSeleccionada);
                med.setHora(horaSeleccionada);
                med.setDescripcion(viewModel.getDescripcion());

                // Guardar en la bd
                MedicamentoRoomDataSource dataSource = MedicamentoRoomDataSource.getInstance(this.getContext());
                dataSource.insert(med, result -> {
                    if (result) { // Retornar a la actividad principal
                        getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra("Medicamento", med));
                        getActivity().finish();
                    } else {
                        // Hubo un error en el guardado, notificar?
                    }
                });
            }
        });

    }

    private boolean verificarDatos() {

        boolean correctos = true;

        // Verificar datos dependiendo de la frecuencia elegida
        switch (frecSeleccionada) {
            case 0: { // Todos los dias
                if (fechaSeleccionada == null) {
                    correctos = false;
                    binding.contFechaHora.errFecha.setVisibility(View.VISIBLE);
                }
                if (horaSeleccionada == null) {
                    correctos = false;
                    binding.contFechaHora.errHora.setVisibility(View.VISIBLE);
                }
            }
            break;
            case 1: { // Intervalos regulares
                if (fechaSeleccionada == null) {
                    correctos = false;
                    binding.contIntRegulares.contFechaHora2.errFecha.setVisibility(View.VISIBLE);
                }
                if (horaSeleccionada == null) {
                    correctos = false;
                    binding.contIntRegulares.contFechaHora2.errHora.setVisibility(View.VISIBLE);
                }
            }
            break;
            case 2: { // Dias de la semana especificos
                if (fechaSeleccionada == null) {
                    correctos = false;
                    binding.contDias.contFechaHora3.errFecha.setVisibility(View.VISIBLE);
                }
                if (horaSeleccionada == null) {
                    correctos = false;
                    binding.contDias.contFechaHora3.errHora.setVisibility(View.VISIBLE);
                }
                if (binding.contDias.diasChipGroup.getCheckedChipIds().size() == 0) {
                    correctos = false;
                    binding.contDias.errDias.setVisibility(View.VISIBLE);
                }
            }
            break;
            case 3: // Segun se necesite
                break;
        }

        return correctos;
    }

    private void setearFechaYHora(FechaHoraBinding binding) {
        // Fecha
        MaterialDatePicker datePicker = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build())
                .setTitleText("Fecha de inicio")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Long time = Long.parseLong(selection.toString()) + 10800000;
            // El date picker toma la fecha UTC con hora 00:00. Le sumo 3 horas (en milisegundos) para que, al guardarlo como
            // ZonedDateTime (ARG - UTC-03:00), siga siendo la misma fecha (y no disminuya un dia por diferencia de 3 horas).
            fechaSeleccionada = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.of("America/Argentina/Buenos_Aires"));
            binding.editFecha.setText(fechaSeleccionada.getDayOfMonth() + "/" + fechaSeleccionada.getMonthValue() + "/" + fechaSeleccionada.getYear());
            binding.errFecha.setVisibility(View.INVISIBLE);
        });

        binding.editFecha.setOnClickListener(v -> {
            datePicker.show(getActivity().getSupportFragmentManager(), "fecha");
        });


        // Hora
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTitleText("Hora")
                .setTimeFormat(DateFormat.is24HourFormat(this.getContext()) ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H) // Dependiendo de como tenga seteada su hora el dispositivo
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            horaSeleccionada = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Utilities.getMiliseconds(timePicker.getHour(), timePicker.getMinute())), ZoneId.of("America/Argentina/Buenos_Aires"));
            binding.editHora.setText(Utilities.formatearHora(timePicker.getHour(), timePicker.getMinute()));
            binding.errHora.setVisibility(View.INVISIBLE);
        });

        binding.editHora.setOnClickListener(v -> {
            timePicker.show(getActivity().getSupportFragmentManager(), "hora");
        });
    }

}