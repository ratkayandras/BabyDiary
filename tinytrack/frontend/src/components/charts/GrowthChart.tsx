import { useTranslation } from "react-i18next";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  ResponsiveContainer, Area, AreaChart,
} from "recharts";
import type { PercentileResult } from "@/types";

interface GrowthChartProps {
  data: PercentileResult[];
  title: string;
  unit: string;
  color?: string;
}

export function GrowthChart({ data, title, unit, color = "#2563eb" }: GrowthChartProps) {
  const { t } = useTranslation();

  if (data.length === 0) {
    return (
      <div className="flex items-center justify-center h-48 text-muted-foreground text-sm">
        {t("common.noData")}
      </div>
    );
  }

  const chartData = data.map((d) => ({
    age: d.ageMonths,
    value: d.value,
    p3: d.bands.p3,
    p15: d.bands.p15,
    p50: d.bands.p50,
    p85: d.bands.p85,
    p97: d.bands.p97,
  }));

  return (
    <div>
      <h3 className="text-sm font-medium mb-2">{title}</h3>
      <ResponsiveContainer width="100%" height={280}>
        <AreaChart data={chartData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
          <defs>
            <linearGradient id="band" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor="#93c5fd" stopOpacity={0.3} />
              <stop offset="95%" stopColor="#93c5fd" stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
          <XAxis
            dataKey="age"
            label={{ value: t("growth.ageMonths"), position: "insideBottom", offset: -5 }}
            tick={{ fontSize: 11 }}
          />
          <YAxis
            label={{ value: unit, angle: -90, position: "insideLeft", offset: 10 }}
            tick={{ fontSize: 11 }}
          />
          <Tooltip
            formatter={(value: number) => [`${value} ${unit}`, ""]}
            labelFormatter={(label) => `${t("growth.ageMonths").replace("(months)", "")}: ${label}m`}
          />
          <Legend wrapperStyle={{ fontSize: "11px" }} />

          {/* WHO percentile bands */}
          <Area type="monotone" dataKey="p97" stroke="#fca5a5" fill="none" strokeDasharray="3 3" strokeWidth={1} name="P97" dot={false} />
          <Area type="monotone" dataKey="p85" stroke="#fcd34d" fill="none" strokeDasharray="3 3" strokeWidth={1} name="P85" dot={false} />
          <Area type="monotone" dataKey="p50" stroke="#86efac" fill="url(#band)" strokeWidth={1.5} name="P50" dot={false} />
          <Area type="monotone" dataKey="p15" stroke="#fcd34d" fill="none" strokeDasharray="3 3" strokeWidth={1} name="P15" dot={false} />
          <Area type="monotone" dataKey="p3" stroke="#fca5a5" fill="none" strokeDasharray="3 3" strokeWidth={1} name="P3" dot={false} />

          {/* Actual measurements */}
          <Line
            type="monotone"
            dataKey="value"
            stroke={color}
            strokeWidth={2.5}
            dot={{ fill: color, r: 4 }}
            name={t("growth.yourChild")}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
