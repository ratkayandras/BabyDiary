import { useState, useMemo } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Download, Trash2, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
} from "recharts";
import { GrowthChart } from "@/components/charts/GrowthChart";
import { childrenApi } from "@/api/children";
import { formatDateTime, formatDuration, downloadBlob, ageFromDob } from "@/lib/utils";
import type { MeasurementType, FeedingType, BreastSide, DiaperType, FeedingLog } from "@/types";

export function ChildProfilePage() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [exportLoading, setExportLoading] = useState<string | null>(null);
  const [deleteOpen, setDeleteOpen] = useState(false);

  const { data: child, isLoading } = useQuery({
    queryKey: ["child", id],
    queryFn: () => childrenApi.get(id!),
    enabled: !!id,
  });

  const { data: growthData } = useQuery({
    queryKey: ["growth", id],
    queryFn: () => childrenApi.growthAnalysis(id!),
    enabled: !!id,
  });

  const handleExport = async (format: "pdf" | "csv") => {
    setExportLoading(format);
    try {
      const resp = await childrenApi.exportData(id!, format);
      const ext = format === "pdf" ? "pdf" : "csv";
      downloadBlob(resp.data as Blob, `${child?.name ?? "export"}.${ext}`);
    } finally {
      setExportLoading(null);
    }
  };

  const deleteMutation = useMutation({
    mutationFn: () => childrenApi.delete(id!),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["children"] });
      navigate("/");
    },
  });

  if (isLoading) return <div className="text-muted-foreground">{t("common.loading")}</div>;
  if (!child) return <div className="text-destructive">{t("errors.notFound")}</div>;

  const age = ageFromDob(child.dateOfBirth);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div className="flex items-center gap-3">
          <Button variant="ghost" size="icon" onClick={() => navigate("/")}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-2xl font-bold">{child.name}</h1>
            <p className="text-sm text-muted-foreground">
              {age.years > 0
                ? t("child.ageYearsMonths", { years: age.years, months: age.months })
                : t("child.ageMonths", { months: age.months })}
              {" · "}
              {t(`child.${child.gender.toLowerCase()}`)}
            </p>
          </div>
        </div>
        <div className="flex gap-2 flex-wrap">
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleExport("csv")}
            disabled={exportLoading === "csv"}
          >
            <Download className="h-4 w-4 mr-1" />
            {exportLoading === "csv" ? t("export.downloading") : t("export.exportCsv")}
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleExport("pdf")}
            disabled={exportLoading === "pdf"}
          >
            <Download className="h-4 w-4 mr-1" />
            {exportLoading === "pdf" ? t("export.downloading") : t("export.exportPdf")}
          </Button>
          <Button
            variant="destructive"
            size="sm"
            onClick={() => setDeleteOpen(true)}
          >
            <Trash2 className="h-4 w-4 mr-1" />
            {t("child.deleteChild")}
          </Button>
        </div>
      </div>

      {/* Delete confirmation dialog */}
      <Dialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("child.deleteChild")}</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            {t("child.confirmDelete", { name: child.name })}
          </p>
          <div className="flex gap-2 justify-end mt-2">
            <Button variant="outline" onClick={() => setDeleteOpen(false)}>{t("common.cancel")}</Button>
            <Button
              variant="destructive"
              onClick={() => deleteMutation.mutate()}
              disabled={deleteMutation.isPending}
            >
              {t("common.delete")}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* Growth Charts */}
      {growthData && (
        <Card>
          <CardHeader>
            <CardTitle>{t("measurements.growthChart")}</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-3">
              <GrowthChart
                data={growthData.weight}
                title={t("measurements.weight")}
                unit="kg"
                color="#2563eb"
              />
              <GrowthChart
                data={growthData.height}
                title={t("measurements.height")}
                unit="cm"
                color="#16a34a"
              />
              <GrowthChart
                data={growthData.headCircumference}
                title={t("measurements.headCircumference")}
                unit="cm"
                color="#9333ea"
              />
            </div>
          </CardContent>
        </Card>
      )}

      {/* Tabs */}
      <Tabs defaultValue="measurements">
        <TabsList className="w-full md:w-auto grid grid-cols-4 md:inline-flex">
          <TabsTrigger value="measurements">{t("measurements.title")}</TabsTrigger>
          <TabsTrigger value="feeding">{t("feeding.title")}</TabsTrigger>
          <TabsTrigger value="diapers">{t("diaper.title")}</TabsTrigger>
          <TabsTrigger value="sleep">{t("sleep.title")}</TabsTrigger>
        </TabsList>

        <TabsContent value="measurements">
          <MeasurementsTab childId={id!} />
        </TabsContent>
        <TabsContent value="feeding">
          <FeedingTab childId={id!} />
        </TabsContent>
        <TabsContent value="diapers">
          <DiaperTab childId={id!} />
        </TabsContent>
        <TabsContent value="sleep">
          <SleepTab childId={id!} />
        </TabsContent>
      </Tabs>
    </div>
  );
}

// ─── Measurements Tab ──────────────────────────────────────────────────────────

function MeasurementsTab({ childId }: { childId: string }) {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [weightUnit, setWeightUnit] = useState<"g" | "kg">("g");
  const [form, setForm] = useState({ type: "WEIGHT" as MeasurementType, value: "", recordedAt: new Date().toISOString().slice(0, 16), notes: "" });

  const { data: measurements = [], isLoading } = useQuery({
    queryKey: ["measurements", childId],
    queryFn: () => childrenApi.listMeasurements(childId),
  });

  const addMutation = useMutation({
    mutationFn: () => childrenApi.addMeasurement(childId, {
      type: form.type,
      value: parseFloat(form.value),
      recordedAt: new Date(form.recordedAt).toISOString(),
      inputUnit: form.type === "WEIGHT" ? weightUnit : undefined,
      notes: form.notes || undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["measurements", childId] });
      qc.invalidateQueries({ queryKey: ["growth", childId] });
      setOpen(false);
      setForm({ type: "WEIGHT", value: "", recordedAt: new Date().toISOString().slice(0, 16), notes: "" });
    },
  });

  const delMutation = useMutation({
    mutationFn: (measurementId: string) => childrenApi.deleteMeasurement(childId, measurementId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["measurements", childId] });
      qc.invalidateQueries({ queryKey: ["growth", childId] });
    },
  });

  const unitFor = (type: MeasurementType) => type === "WEIGHT" ? weightUnit : "cm";

  return (
    <Card>
      <CardHeader className="flex-row justify-between items-center">
        <CardTitle className="text-base">{t("measurements.title")}</CardTitle>
        <Button size="sm" onClick={() => setOpen(true)}>
          <Plus className="h-4 w-4 mr-1" />{t("measurements.addMeasurement")}
        </Button>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <p className="text-muted-foreground text-sm">{t("common.loading")}</p>
        ) : measurements.length === 0 ? (
          <p className="text-muted-foreground text-sm">{t("measurements.noMeasurements")}</p>
        ) : (
          <div className="space-y-2">
            {measurements.map((m) => (
              <div key={m.id} className="flex items-center justify-between py-2 border-b last:border-0">
                <div>
                  <span className="font-medium text-sm">{t(`measurements.${m.type.toLowerCase().replace("_", "")}`) || m.type}</span>
                  <span className="text-muted-foreground text-sm ml-2">{Number(m.value)} {m.unit}</span>
                  <span className="text-xs text-muted-foreground ml-2">· {formatDateTime(m.recordedAt)}</span>
                </div>
                <Button variant="ghost" size="icon" onClick={() => delMutation.mutate(m.id)}>
                  <Trash2 className="h-4 w-4 text-destructive" />
                </Button>
              </div>
            ))}
          </div>
        )}
      </CardContent>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{t("measurements.addMeasurement")}</DialogTitle></DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>{t("measurements.type")}</Label>
              <Select value={form.type} onValueChange={(v) => setForm(f => ({ ...f, type: v as MeasurementType }))}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="WEIGHT">{t("measurements.weight")}</SelectItem>
                  <SelectItem value="HEIGHT">{t("measurements.height")}</SelectItem>
                  <SelectItem value="HEAD_CIRCUMFERENCE">{t("measurements.headCircumference")}</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {form.type === "WEIGHT" && (
              <div className="space-y-2">
                <Label>{t("measurements.unit")}</Label>
                <div className="flex gap-2">
                  <Button
                    type="button"
                    variant={weightUnit === "g" ? "default" : "outline"}
                    size="sm"
                    onClick={() => setWeightUnit("g")}
                  >g</Button>
                  <Button
                    type="button"
                    variant={weightUnit === "kg" ? "default" : "outline"}
                    size="sm"
                    onClick={() => setWeightUnit("kg")}
                  >kg</Button>
                </div>
              </div>
            )}
            <div className="space-y-2">
              <Label>{t("measurements.value")} ({unitFor(form.type)})</Label>
              <Input
                type="number"
                step={form.type === "WEIGHT" && weightUnit === "g" ? "1" : "0.1"}
                value={form.value}
                onChange={(e) => setForm(f => ({ ...f, value: e.target.value }))}
              />
            </div>
            <div className="space-y-2">
              <Label>{t("measurements.recordedAt")}</Label>
              <Input type="datetime-local" value={form.recordedAt} onChange={(e) => setForm(f => ({ ...f, recordedAt: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <Label>{t("measurements.notes")} ({t("common.optional")})</Label>
              <Textarea value={form.notes} onChange={(e) => setForm(f => ({ ...f, notes: e.target.value }))} />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setOpen(false)}>{t("common.cancel")}</Button>
              <Button onClick={() => addMutation.mutate()} disabled={!form.value || addMutation.isPending}>
                {t("common.save")}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  );
}

// ─── Feeding Chart ─────────────────────────────────────────────────────────────

function FeedingChart({ logs }: { logs: FeedingLog[] }) {
  const { t } = useTranslation();

  const chartData = useMemo(() => {
    // Determine milk vs formula ml per feeding log
    const byDay = new Map<string, { milk: number; formula: number; count: number }>();

    logs.forEach((log) => {
      const day = log.startTime.slice(0, 10); // YYYY-MM-DD
      const entry = byDay.get(day) ?? { milk: 0, formula: 0, count: 0 };

      if (log.type === "BREAST" || log.type === "EXPRESSED") {
        entry.milk += log.amountMl ?? 0;
        entry.formula += log.formulaAmountMl ?? 0;
      } else if (log.type === "FORMULA") {
        // Legacy: amountMl stores formula. formulaAmountMl takes precedence if set.
        entry.formula += log.formulaAmountMl ?? log.amountMl ?? 0;
      }
      entry.count += 1;
      byDay.set(day, entry);
    });

    return Array.from(byDay.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .slice(-30) // last 30 days
      .map(([day, { milk, formula, count }]) => ({
        day: day.slice(5), // MM-DD
        [t("feeding.motherMilk")]: Math.round(milk),
        [t("feeding.formulaTotal")]: Math.round(formula),
        total: Math.round(milk + formula),
        count,
      }));
  }, [logs, t]);

  if (chartData.length === 0) return null;

  return (
    <Card className="mb-4">
      <CardHeader>
        <CardTitle className="text-base">{t("feeding.chart")}</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={chartData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis dataKey="day" tick={{ fontSize: 11 }} />
            <YAxis
              label={{ value: t("feeding.dailyIntake"), angle: -90, position: "insideLeft", offset: 15, style: { fontSize: 10 } }}
              tick={{ fontSize: 11 }}
            />
            <Tooltip
              formatter={(val: number, name: string) => [`${val} ml`, name]}
            />
            <Legend wrapperStyle={{ fontSize: "11px" }} />
            <Bar dataKey={t("feeding.motherMilk")} stackId="a" fill="#2563eb" />
            <Bar dataKey={t("feeding.formulaTotal")} stackId="a" fill="#f97316" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}

// ─── Feeding Tab ───────────────────────────────────────────────────────────────

function FeedingTab({ childId }: { childId: string }) {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({
    type: "BREAST" as FeedingType,
    startTime: new Date().toISOString().slice(0, 16),
    endTime: "",
    amountMl: "",
    formulaAmountMl: "",
    side: "" as BreastSide | "",
    notes: "",
  });

  // Fetch a larger set for the chart (separate query)
  const { data: allLogs = [] } = useQuery({
    queryKey: ["feeding-logs-chart", childId],
    queryFn: () => childrenApi.listFeedingLogs(childId, 500),
  });

  const { data: logs = [], isLoading } = useQuery({
    queryKey: ["feeding-logs", childId],
    queryFn: () => childrenApi.listFeedingLogs(childId),
  });

  const addMutation = useMutation({
    mutationFn: () => childrenApi.addFeedingLog(childId, {
      type: form.type,
      startTime: new Date(form.startTime).toISOString(),
      endTime: form.endTime ? new Date(form.endTime).toISOString() : undefined,
      amountMl: form.amountMl ? parseFloat(form.amountMl) : undefined,
      formulaAmountMl: form.formulaAmountMl ? parseFloat(form.formulaAmountMl) : undefined,
      side: (form.side || undefined) as BreastSide | undefined,
      notes: form.notes || undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["feeding-logs", childId] });
      qc.invalidateQueries({ queryKey: ["feeding-logs-chart", childId] });
      setOpen(false);
      setForm(f => ({ ...f, amountMl: "", formulaAmountMl: "", endTime: "", side: "", notes: "" }));
    },
  });

  const delMutation = useMutation({
    mutationFn: (logId: string) => childrenApi.deleteFeedingLog(childId, logId),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["feeding-logs", childId] });
      qc.invalidateQueries({ queryKey: ["feeding-logs-chart", childId] });
    },
  });

  const showMilkAmount = form.type === "BREAST" || form.type === "EXPRESSED";
  const showFormulaAmount = form.type === "FORMULA" || form.type === "BREAST" || form.type === "EXPRESSED";
  const showSide = form.type === "BREAST";

  return (
    <>
      <FeedingChart logs={allLogs} />
      <Card>
        <CardHeader className="flex-row justify-between items-center">
          <CardTitle className="text-base">{t("feeding.title")}</CardTitle>
          <Button size="sm" onClick={() => setOpen(true)}>
            <Plus className="h-4 w-4 mr-1" />{t("feeding.addFeeding")}
          </Button>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p className="text-muted-foreground text-sm">{t("common.loading")}</p>
          ) : logs.length === 0 ? (
            <p className="text-muted-foreground text-sm">{t("feeding.noLogs")}</p>
          ) : (
            <div className="space-y-2">
              {logs.map((log) => (
                <div key={log.id} className="flex items-center justify-between py-2 border-b last:border-0">
                  <div>
                    <span className="font-medium text-sm">{t(`feeding.${log.type.toLowerCase()}`)}</span>
                    <span className="text-xs text-muted-foreground ml-2">· {formatDateTime(log.startTime)}</span>
                    {log.endTime && (
                      <span className="text-xs text-muted-foreground ml-1">({formatDuration(log.startTime, log.endTime)})</span>
                    )}
                    {log.amountMl != null && (log.type === "BREAST" || log.type === "EXPRESSED") && (
                      <span className="text-xs text-muted-foreground ml-1">· {log.amountMl}ml {t("feeding.motherMilk").toLowerCase()}</span>
                    )}
                    {log.amountMl != null && log.type === "FORMULA" && (
                      <span className="text-xs text-muted-foreground ml-1">· {log.amountMl}ml</span>
                    )}
                    {log.formulaAmountMl != null && (
                      <span className="text-xs text-muted-foreground ml-1">+ {log.formulaAmountMl}ml {t("feeding.formulaTotal").toLowerCase()}</span>
                    )}
                    {log.side && <span className="text-xs text-muted-foreground ml-1">· {t(`feeding.${log.side.toLowerCase()}`)}</span>}
                  </div>
                  <Button variant="ghost" size="icon" onClick={() => delMutation.mutate(log.id)}>
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>

        <Dialog open={open} onOpenChange={setOpen}>
          <DialogContent>
            <DialogHeader><DialogTitle>{t("feeding.addFeeding")}</DialogTitle></DialogHeader>
            <div className="space-y-4">
              <div className="space-y-2">
                <Label>{t("feeding.type")}</Label>
                <Select value={form.type} onValueChange={(v) => setForm(f => ({ ...f, type: v as FeedingType, side: "" }))}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="BREAST">{t("feeding.breast")}</SelectItem>
                    <SelectItem value="EXPRESSED">{t("feeding.expressed")}</SelectItem>
                    <SelectItem value="FORMULA">{t("feeding.formula")}</SelectItem>
                    <SelectItem value="SOLID">{t("feeding.solid")}</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label>{t("feeding.startTime")}</Label>
                  <Input type="datetime-local" value={form.startTime} onChange={(e) => setForm(f => ({ ...f, startTime: e.target.value }))} />
                </div>
                <div className="space-y-2">
                  <Label>{t("feeding.endTime")} ({t("common.optional")})</Label>
                  <Input type="datetime-local" value={form.endTime} onChange={(e) => setForm(f => ({ ...f, endTime: e.target.value }))} />
                </div>
              </div>
              {showSide && (
                <div className="space-y-2">
                  <Label>{t("feeding.side")} ({t("common.optional")})</Label>
                  <Select value={form.side} onValueChange={(v) => setForm(f => ({ ...f, side: v as BreastSide }))}>
                    <SelectTrigger><SelectValue placeholder="—" /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="LEFT">{t("feeding.left")}</SelectItem>
                      <SelectItem value="RIGHT">{t("feeding.right")}</SelectItem>
                      <SelectItem value="BOTH">{t("feeding.both")}</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}
              {showMilkAmount && (
                <div className="space-y-2">
                  <Label>{t("feeding.amount")} ({t("common.optional")})</Label>
                  <Input type="number" min="0" step="1" value={form.amountMl} onChange={(e) => setForm(f => ({ ...f, amountMl: e.target.value }))} />
                </div>
              )}
              {showFormulaAmount && (
                <div className="space-y-2">
                  <Label>
                    {form.type === "FORMULA" ? t("feeding.amount") : t("feeding.formulaAmount")}
                    {" "}({t("common.optional")})
                  </Label>
                  <Input type="number" min="0" step="1" value={form.formulaAmountMl} onChange={(e) => setForm(f => ({ ...f, formulaAmountMl: e.target.value }))} />
                </div>
              )}
              <div className="space-y-2">
                <Label>{t("feeding.notes")} ({t("common.optional")})</Label>
                <Textarea value={form.notes} onChange={(e) => setForm(f => ({ ...f, notes: e.target.value }))} />
              </div>
              <div className="flex gap-2 justify-end">
                <Button variant="outline" onClick={() => setOpen(false)}>{t("common.cancel")}</Button>
                <Button onClick={() => addMutation.mutate()} disabled={addMutation.isPending}>
                  {t("common.save")}
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </Card>
    </>
  );
}

// ─── Diaper Tab ────────────────────────────────────────────────────────────────

function DiaperTab({ childId }: { childId: string }) {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ type: "WET" as DiaperType, recordedAt: new Date().toISOString().slice(0, 16), notes: "" });

  const { data: logs = [], isLoading } = useQuery({
    queryKey: ["diaper-logs", childId],
    queryFn: () => childrenApi.listDiaperLogs(childId),
  });

  const addMutation = useMutation({
    mutationFn: () => childrenApi.addDiaperLog(childId, {
      type: form.type,
      recordedAt: new Date(form.recordedAt).toISOString(),
      notes: form.notes || undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["diaper-logs", childId] });
      setOpen(false);
    },
  });

  const delMutation = useMutation({
    mutationFn: (logId: string) => childrenApi.deleteDiaperLog(childId, logId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["diaper-logs", childId] }),
  });

  return (
    <Card>
      <CardHeader className="flex-row justify-between items-center">
        <CardTitle className="text-base">{t("diaper.title")}</CardTitle>
        <Button size="sm" onClick={() => setOpen(true)}>
          <Plus className="h-4 w-4 mr-1" />{t("diaper.addDiaper")}
        </Button>
      </CardHeader>
      <CardContent>
        {isLoading ? <p className="text-muted-foreground text-sm">{t("common.loading")}</p>
          : logs.length === 0 ? <p className="text-muted-foreground text-sm">{t("diaper.noLogs")}</p>
          : (
            <div className="space-y-2">
              {logs.map((log) => (
                <div key={log.id} className="flex items-center justify-between py-2 border-b last:border-0">
                  <div>
                    <span className="font-medium text-sm">{t(`diaper.${log.type.toLowerCase()}`)}</span>
                    <span className="text-xs text-muted-foreground ml-2">· {formatDateTime(log.recordedAt)}</span>
                    {log.notes && <span className="text-xs text-muted-foreground ml-1">· {log.notes}</span>}
                  </div>
                  <Button variant="ghost" size="icon" onClick={() => delMutation.mutate(log.id)}>
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          )}
      </CardContent>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{t("diaper.addDiaper")}</DialogTitle></DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>{t("diaper.type")}</Label>
              <Select value={form.type} onValueChange={(v) => setForm(f => ({ ...f, type: v as DiaperType }))}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="WET">{t("diaper.wet")}</SelectItem>
                  <SelectItem value="DIRTY">{t("diaper.dirty")}</SelectItem>
                  <SelectItem value="BOTH">{t("diaper.both")}</SelectItem>
                  <SelectItem value="DRY">{t("diaper.dry")}</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>{t("diaper.recordedAt")}</Label>
              <Input type="datetime-local" value={form.recordedAt} onChange={(e) => setForm(f => ({ ...f, recordedAt: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <Label>{t("diaper.notes")} ({t("common.optional")})</Label>
              <Textarea value={form.notes} onChange={(e) => setForm(f => ({ ...f, notes: e.target.value }))} />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setOpen(false)}>{t("common.cancel")}</Button>
              <Button onClick={() => addMutation.mutate()} disabled={addMutation.isPending}>{t("common.save")}</Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  );
}

// ─── Sleep Tab ─────────────────────────────────────────────────────────────────

function SleepTab({ childId }: { childId: string }) {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ startTime: new Date().toISOString().slice(0, 16), endTime: "", notes: "" });

  const { data: logs = [], isLoading } = useQuery({
    queryKey: ["sleep-logs", childId],
    queryFn: () => childrenApi.listSleepLogs(childId),
  });

  const addMutation = useMutation({
    mutationFn: () => childrenApi.addSleepLog(childId, {
      startTime: new Date(form.startTime).toISOString(),
      endTime: form.endTime ? new Date(form.endTime).toISOString() : undefined,
      notes: form.notes || undefined,
    }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["sleep-logs", childId] });
      setOpen(false);
    },
  });

  const delMutation = useMutation({
    mutationFn: (logId: string) => childrenApi.deleteSleepLog(childId, logId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["sleep-logs", childId] }),
  });

  return (
    <Card>
      <CardHeader className="flex-row justify-between items-center">
        <CardTitle className="text-base">{t("sleep.title")}</CardTitle>
        <Button size="sm" onClick={() => setOpen(true)}>
          <Plus className="h-4 w-4 mr-1" />{t("sleep.addSleep")}
        </Button>
      </CardHeader>
      <CardContent>
        {isLoading ? <p className="text-muted-foreground text-sm">{t("common.loading")}</p>
          : logs.length === 0 ? <p className="text-muted-foreground text-sm">{t("sleep.noLogs")}</p>
          : (
            <div className="space-y-2">
              {logs.map((log) => (
                <div key={log.id} className="flex items-center justify-between py-2 border-b last:border-0">
                  <div>
                    <span className="text-sm">{formatDateTime(log.startTime)}</span>
                    {log.endTime && (
                      <>
                        <span className="text-xs text-muted-foreground ml-1">→ {formatDateTime(log.endTime)}</span>
                        <span className="text-xs text-muted-foreground ml-1">({formatDuration(log.startTime, log.endTime)})</span>
                      </>
                    )}
                    {!log.endTime && <span className="text-xs text-muted-foreground ml-1">(ongoing)</span>}
                  </div>
                  <Button variant="ghost" size="icon" onClick={() => delMutation.mutate(log.id)}>
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              ))}
            </div>
          )}
      </CardContent>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{t("sleep.addSleep")}</DialogTitle></DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>{t("sleep.startTime")}</Label>
              <Input type="datetime-local" value={form.startTime} onChange={(e) => setForm(f => ({ ...f, startTime: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <Label>{t("sleep.endTime")} ({t("common.optional")})</Label>
              <Input type="datetime-local" value={form.endTime} onChange={(e) => setForm(f => ({ ...f, endTime: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <Label>{t("sleep.notes")} ({t("common.optional")})</Label>
              <Textarea value={form.notes} onChange={(e) => setForm(f => ({ ...f, notes: e.target.value }))} />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setOpen(false)}>{t("common.cancel")}</Button>
              <Button onClick={() => addMutation.mutate()} disabled={addMutation.isPending}>{t("common.save")}</Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </Card>
  );
}
