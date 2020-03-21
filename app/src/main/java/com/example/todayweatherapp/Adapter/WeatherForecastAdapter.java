package com.example.todayweatherapp.Adapter;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.recyclerview.widget.RecyclerView;

        import com.example.todayweatherapp.Common.Common;
        import com.example.todayweatherapp.ForecastFragment;
        import com.example.todayweatherapp.Model.WeatherForecastResult;
        import com.example.todayweatherapp.R;
        import com.squareup.picasso.Picasso;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder> {
    Context context;
    WeatherForecastResult weatherForecastResult;



    public WeatherForecastAdapter(Context context, WeatherForecastResult weatherForecastResult) {
        this.context = context;
        this.weatherForecastResult = weatherForecastResult;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_weather_forecast, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherForecastResult.getList().get(position).getWeather().get(0).getIcon())
                .append(".png").toString()).into(holder.img_weather);
        holder.txt_date.setText(new StringBuilder(Common.convertUnixToDateTime(weatherForecastResult
                .getList().get(position).getDt())));

        holder.txt_description.setText(new StringBuilder(weatherForecastResult.getList()
                .get(position).getWeather().get(0).getDescription()));
        holder.txt_temp.setText(new StringBuilder(String.valueOf(weatherForecastResult
                .getList().get(position).getMain().getTemp())).append("°C"));

    }

    @Override
    public int getItemCount() {
        return weatherForecastResult.getList().size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txt_date, txt_description, txt_temp;
        ImageView img_weather;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img_weather = itemView.findViewById(R.id.img_weather);
            txt_date = itemView.findViewById(R.id.txt_date);
            txt_description = itemView.findViewById(R.id.txt_description);
            txt_temp = itemView.findViewById(R.id.txt_temp);
    }
    }
}
