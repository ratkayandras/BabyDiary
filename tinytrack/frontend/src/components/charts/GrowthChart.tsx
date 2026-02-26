import { useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
  ComposedChart, Line, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  Legend, ResponsiveContainer,
} from "recharts";
import type { GrowthSeries } from "@/types";

interface GrowthChartProps {
  data: GrowthSeries;
  title: string;
  unit: string;
  color?: string;
}

export function GrowthChart({ data, title, unit, color = "#2563eb" }: GrowthChartProps) {
  const { t } = useTranslation();

  // Merge WHO reference bands (full 0-60 month curve) with child measurement points.
  // Every row has the band values; rows that coincide with a measurement also have `value`.
  const chartData = useMemo(() => {
    const byAge = new Map<number, number>();
    data.measurements.forEach((m) => {
      byAge.set(m.ageMonths, m.value);
    });

    return data.bands.map((b) => ({
      month: b.month,
      p3: b.p3,
      p15: b.p15,
      p50: b.p50,
      p85: b.p85,
      p97: b.p97,
      value: byAge.has(b.month) ? byAge.get(b.month) : null,
    }));
  }, [data]);

  if (data.bands.length === 0) {
    return (
      <div className="flex items-center justify-center h-48 text-muted-foreground text-sm">
        {t("common.noData")}
      </div>
    );
  }

  const hasData = data.measurements.length > 0;

  return (
    <div>
      <h3 className="text-sm font-medium mb-2">{title}</h3>
      <ResponsiveContainer width="100%" height={280}>
        <ComposedChart data={chartData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
          <defs>
            <linearGradient id={`band-${title}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#93c5fd" stopOpacity={0.25} />
              <stop offset="95%" stopColor="#93c5fd" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
          <XAxis
            dataKey="month"
            label={{ value: t("growth.ageMonths"), position: "insideBottom", offset: -5 }}
            tick={{ fontSize: 11 }}
          />
          <YAxis
            label={{ value: unit, angle: -90, position: "insideLeft", offset: 10 }}
            tick={{ fontSize: 11 }}
            domain={["auto", "auto"]}
          />
          <Tooltip
            formatter={(val: number | null, name: string) => {
              if (val === null || val === undefined) return ["-", name];
              return [`${Number(val).toFixed(2)} ${unit}`, name];
            }}
            labelFormatter={(label) => `${t("growth.ageMonths")}: ${label}m`}
          />
          <Legend wrapperStyle={{ fontSize: "11px" }} />

          {/* WHO percentile reference bands — continuous curves across 0-60 months */}
          <Area type="monotone" dataKey="p97" stroke="#fca5a5" fill="none" strokeDasharray="4 2" strokeWidth={1} name="P97" dot={false} legendType="line" />
          <Area type="monotone" dataKey="p85" stroke="#fcd34d" fill="none" strokeDasharray="4 2" strokeWidth={1} name="P85" dot={false} legendType="line" />
          <Area type="monotone" dataKey="p50" stroke="#86efac" fill={`url(#band-${title})`} strokeWidth={1.5} name="P50" dot={false} legendType="line" />
          <Area type="monotone" dataKey="p15" stroke="#fcd34d" fill="none" strokeDasharray="4 2" strokeWidth={1} name="P15" dot={false} legendType="line" />
          <Area type="monotone" dataKey="p3" stroke="#fca5a5" fill="none" strokeDasharray="4 2" strokeWidth={1} name="P3" dot={false} legendType="line" />

          {/* Child's actual measurements overlaid as dots (connected if multiple) */}
          {hasData && (
            <Line
              type="monotone"
              dataKey="value"
              stroke={color}
              strokeWidth={2.5}
              dot={{ fill: color, r: 5, strokeWidth: 2, stroke: "#fff" }}
              activeDot={{ r: 7 }}
              connectNulls={false}
              name={t("growth.yourChild")}
            />
          )}
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
