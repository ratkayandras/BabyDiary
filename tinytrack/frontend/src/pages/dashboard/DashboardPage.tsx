import { useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Baby, Utensils, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useAuth } from "@/hooks/useAuth";
import { childrenApi, familiesApi } from "@/api/children";
import { ageFromDob, formatDateTime } from "@/lib/utils";
import type { Child, Family, Gender } from "@/types";
import type { AxiosError } from "axios";

export function DashboardPage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const qc = useQueryClient();
  const [addChildOpen, setAddChildOpen] = useState(false);
  const [addFamilyOpen, setAddFamilyOpen] = useState(false);

  const { data: children = [], isLoading: childrenLoading } = useQuery({
    queryKey: ["children"],
    queryFn: childrenApi.list,
  });

  const { data: families = [] } = useQuery<Family[]>({
    queryKey: ["families"],
    queryFn: familiesApi.list,
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{t("dashboard.title")}</h1>
          <p className="text-muted-foreground">{t("dashboard.welcome", { name: user?.name })}</p>
        </div>
        <div className="flex gap-2">
          {families.length === 0 && (
            <Button variant="outline" size="sm" onClick={() => setAddFamilyOpen(true)}>
              <Plus className="h-4 w-4 mr-1" />
              {t("family.createFamily")}
            </Button>
          )}
          <Button size="sm" onClick={() => setAddChildOpen(true)} disabled={families.length === 0}>
            <Plus className="h-4 w-4 mr-1" />
            {t("dashboard.addChild")}
          </Button>
        </div>
      </div>

      {families.length === 0 && (
        <Card className="border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-12 text-center gap-3">
            <AlertCircle className="h-10 w-10 text-muted-foreground" />
            <p className="text-muted-foreground">{t("family.noFamilies")}</p>
            <Button onClick={() => setAddFamilyOpen(true)}>
              <Plus className="h-4 w-4 mr-1" />
              {t("family.createFamily")}
            </Button>
          </CardContent>
        </Card>
      )}

      {childrenLoading ? (
        <div className="text-muted-foreground">{t("common.loading")}</div>
      ) : children.length === 0 && families.length > 0 ? (
        <Card className="border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-12 text-center gap-3">
            <Baby className="h-10 w-10 text-muted-foreground" />
            <p className="text-muted-foreground">{t("dashboard.noChildren")}</p>
            <p className="text-sm text-muted-foreground">{t("dashboard.addFirstChild")}</p>
            <Button onClick={() => setAddChildOpen(true)}>
              <Plus className="h-4 w-4 mr-1" />
              {t("dashboard.addChild")}
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {children.map((child) => (
            <ChildCard key={child.id} child={child} />
          ))}
        </div>
      )}

      <AddFamilyDialog
        open={addFamilyOpen}
        onClose={() => setAddFamilyOpen(false)}
        onSuccess={() => { setAddFamilyOpen(false); qc.invalidateQueries({ queryKey: ["families"] }); }}
      />

      <AddChildDialog
        open={addChildOpen}
        onClose={() => setAddChildOpen(false)}
        families={families}
        onSuccess={() => { setAddChildOpen(false); qc.invalidateQueries({ queryKey: ["children"] }); }}
      />
    </div>
  );
}

function ChildCard({ child }: { child: Child }) {
  const { t } = useTranslation();
  const age = ageFromDob(child.dateOfBirth);

  const { data: feedingLogs } = useQuery({
    queryKey: ["feeding-logs", child.id, 1],
    queryFn: () => childrenApi.listFeedingLogs(child.id, 1),
  });

  const lastFeeding = feedingLogs?.[0];

  return (
    <Link to={`/children/${child.id}`}>
      <Card className="hover:shadow-md transition-shadow cursor-pointer h-full">
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center overflow-hidden">
              {child.photoUrl ? (
                <img src={child.photoUrl} alt={child.name} className="h-full w-full object-cover" />
              ) : (
                <Baby className="h-6 w-6 text-primary" />
              )}
            </div>
            <div>
              <CardTitle className="text-lg">{child.name}</CardTitle>
              <p className="text-sm text-muted-foreground">
                {age.years > 0
                  ? t("child.ageYearsMonths", { years: age.years, months: age.months })
                  : t("child.ageMonths", { months: age.months })}
              </p>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Utensils className="h-4 w-4" />
            {lastFeeding
              ? `${t("dashboard.lastFeeding")}: ${formatDateTime(lastFeeding.startTime)}`
              : t("dashboard.noRecent")}
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}

function AddFamilyDialog({ open, onClose, onSuccess }: {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const { t } = useTranslation();
  const [name, setName] = useState("");
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (n: string) => familiesApi.create(n),
    onSuccess,
    onError: (e: AxiosError<{ message: string }>) => setError(e.response?.data?.message ?? t("errors.generic")),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("family.createFamily")}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          {error && <p className="text-sm text-destructive">{error}</p>}
          <div className="space-y-2">
            <Label>{t("family.familyName")}</Label>
            <Input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="flex gap-2 justify-end">
            <Button variant="outline" onClick={onClose}>{t("common.cancel")}</Button>
            <Button onClick={() => mutation.mutate(name)} disabled={!name || mutation.isPending}>
              {t("common.save")}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

function AddChildDialog({ open, onClose, families, onSuccess }: {
  open: boolean;
  onClose: () => void;
  families: Family[];
  onSuccess: () => void;
}) {
  const { t } = useTranslation();
  const [form, setForm] = useState({ name: "", dateOfBirth: "", gender: "OTHER" as Gender, familyId: families[0]?.id || "" });
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: () => childrenApi.create({
      familyId: form.familyId,
      name: form.name,
      dateOfBirth: form.dateOfBirth,
      gender: form.gender,
    }),
    onSuccess,
    onError: (e: AxiosError<{ message: string }>) => setError(e.response?.data?.message ?? t("errors.generic")),
  });

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("child.addChild")}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          {error && <p className="text-sm text-destructive">{error}</p>}
          {families.length > 1 && (
            <div className="space-y-2">
              <Label>{t("family.title")}</Label>
              <Select value={form.familyId} onValueChange={(v) => setForm(f => ({ ...f, familyId: v }))}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {families.map((fam) => (
                    <SelectItem key={fam.id} value={fam.id}>{fam.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
          <div className="space-y-2">
            <Label>{t("child.name")}</Label>
            <Input value={form.name} onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))} />
          </div>
          <div className="space-y-2">
            <Label>{t("child.dateOfBirth")}</Label>
            <Input type="date" value={form.dateOfBirth} onChange={(e) => setForm(f => ({ ...f, dateOfBirth: e.target.value }))} />
          </div>
          <div className="space-y-2">
            <Label>{t("child.gender")}</Label>
            <Select value={form.gender} onValueChange={(v) => setForm(f => ({ ...f, gender: v as Gender }))}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="MALE">{t("child.male")}</SelectItem>
                <SelectItem value="FEMALE">{t("child.female")}</SelectItem>
                <SelectItem value="OTHER">{t("child.other")}</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="flex gap-2 justify-end">
            <Button variant="outline" onClick={onClose}>{t("common.cancel")}</Button>
            <Button onClick={() => mutation.mutate()} disabled={!form.name || !form.dateOfBirth || mutation.isPending}>
              {t("common.save")}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
