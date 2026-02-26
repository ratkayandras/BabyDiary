import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { useAuth } from "@/hooks/useAuth";
import { authApi } from "@/api/auth";
import { familiesApi } from "@/api/children";
import type { Family, Language } from "@/types";
import type { AxiosError } from "axios";
import i18n from "@/i18n/index";

export function SettingsPage() {
  const { t } = useTranslation();
  const { user, updateUser } = useAuth();
  const qc = useQueryClient();

  const [name, setName] = useState(user?.name ?? "");
  const [language, setLanguage] = useState<Language>(user?.preferredLanguage ?? "EN");
  const [currentPw, setCurrentPw] = useState("");
  const [newPw, setNewPw] = useState("");
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState(false);

  const [inviteOpen, setInviteOpen] = useState(false);
  const [selectedFamilyId, setSelectedFamilyId] = useState<string | null>(null);

  const { data: families = [] } = useQuery<Family[]>({
    queryKey: ["families"],
    queryFn: familiesApi.list,
  });

  const saveMutation = useMutation({
    mutationFn: () => authApi.updateMe({
      name,
      preferredLanguage: language,
      currentPassword: currentPw || undefined,
      newPassword: newPw || undefined,
    }),
    onSuccess: (updated) => {
      updateUser(updated);
      i18n.changeLanguage(updated.preferredLanguage.toLowerCase());
      localStorage.setItem("language", updated.preferredLanguage.toLowerCase());
      setSaveSuccess(true);
      setSaveError(null);
      setCurrentPw("");
      setNewPw("");
      setTimeout(() => setSaveSuccess(false), 3000);
    },
    onError: (e: AxiosError<{ message: string }>) => {
      setSaveError(e.response?.data?.message ?? t("errors.generic"));
    },
  });

  return (
    <div className="space-y-6 max-w-2xl">
      <h1 className="text-2xl font-bold">{t("settings.title")}</h1>

      {/* Account Section */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("settings.account")}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {saveError && (
            <div className="text-sm text-destructive bg-destructive/10 px-3 py-2 rounded-md">{saveError}</div>
          )}
          {saveSuccess && (
            <div className="text-sm text-green-700 bg-green-50 px-3 py-2 rounded-md">
              Saved successfully!
            </div>
          )}
          <div className="space-y-2">
            <Label>{t("auth.email")}</Label>
            <Input value={user?.email ?? ""} disabled />
          </div>
          <div className="space-y-2">
            <Label>{t("auth.name")}</Label>
            <Input value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label>{t("settings.language")}</Label>
            <Select value={language} onValueChange={(v) => setLanguage(v as Language)}>
              <SelectTrigger className="w-48">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="EN">{t("settings.english")}</SelectItem>
                <SelectItem value="HU">{t("settings.hungarian")}</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Password Section */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t("settings.changePassword")}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label>{t("settings.currentPassword")}</Label>
            <Input type="password" value={currentPw} onChange={(e) => setCurrentPw(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label>{t("settings.newPassword")}</Label>
            <Input type="password" value={newPw} onChange={(e) => setNewPw(e.target.value)} />
          </div>
        </CardContent>
      </Card>

      <Button
        onClick={() => saveMutation.mutate()}
        disabled={saveMutation.isPending}
      >
        {saveMutation.isPending ? t("settings.saving") : t("settings.saveChanges")}
      </Button>

      {/* Family Management Section */}
      <Card>
        <CardHeader className="flex-row justify-between items-center">
          <CardTitle className="text-base">{t("settings.familyManagement")}</CardTitle>
          {families.length > 0 && (
            <Button size="sm" variant="outline" onClick={() => {
              setSelectedFamilyId(families[0].id);
              setInviteOpen(true);
            }}>
              {t("family.inviteMember")}
            </Button>
          )}
        </CardHeader>
        <CardContent>
          {families.length === 0 ? (
            <p className="text-sm text-muted-foreground">{t("family.noFamilies")}</p>
          ) : (
            <div className="space-y-6">
              {families.map((family) => (
                <FamilySection
                  key={family.id}
                  family={family}
                  onInvite={() => { setSelectedFamilyId(family.id); setInviteOpen(true); }}
                />
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <InviteDialog
        open={inviteOpen}
        familyId={selectedFamilyId ?? ""}
        onClose={() => setInviteOpen(false)}
        onSuccess={() => { setInviteOpen(false); qc.invalidateQueries({ queryKey: ["families"] }); }}
      />
    </div>
  );
}

function FamilySection({ family, onInvite }: { family: Family; onInvite: () => void }) {
  const { t } = useTranslation();
  return (
    <div>
      <div className="flex items-center justify-between mb-2">
        <h3 className="font-medium">{family.name}</h3>
        <Button size="sm" variant="outline" onClick={onInvite}>
          {t("family.inviteMember")}
        </Button>
      </div>
      <div className="space-y-1">
        {family.members.map((m) => (
          <div key={m.userId} className="flex items-center justify-between text-sm py-1.5 border-b last:border-0">
            <div>
              <span className="font-medium">{m.name}</span>
              <span className="text-muted-foreground ml-2">{m.email}</span>
            </div>
            <span className="text-xs text-muted-foreground bg-secondary px-2 py-0.5 rounded">
              {t(`family.role.${m.role}`)}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function InviteDialog({ open, familyId, onClose, onSuccess }: {
  open: boolean;
  familyId: string;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const { t } = useTranslation();
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);

  const mutation = useMutation({
    mutationFn: () => familiesApi.invite(familyId, email),
    onSuccess: () => { setSent(true); setTimeout(onSuccess, 1500); },
    onError: (e: AxiosError<{ message: string }>) => setError(e.response?.data?.message ?? t("errors.generic")),
  });

  const handleClose = () => {
    setEmail("");
    setError(null);
    setSent(false);
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && handleClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t("family.inviteMember")}</DialogTitle>
        </DialogHeader>
        {sent ? (
          <p className="text-sm text-green-700 bg-green-50 px-3 py-2 rounded-md">
            Invitation sent!
          </p>
        ) : (
          <div className="space-y-4">
            {error && <p className="text-sm text-destructive">{error}</p>}
            <div className="space-y-2">
              <Label>{t("family.inviteEmail")}</Label>
              <Input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="partner@example.com"
              />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={handleClose}>{t("common.cancel")}</Button>
              <Button onClick={() => mutation.mutate()} disabled={!email || mutation.isPending}>
                {t("family.inviteMember")}
              </Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
