import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Baby, Settings, LogOut, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/hooks/useAuth";
import { useState } from "react";

export function Navbar() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  return (
    <nav className="border-b bg-background sticky top-0 z-40">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 font-bold text-lg text-primary">
          <Baby className="h-6 w-6" />
          <span>{t("app.name")}</span>
        </Link>

        {/* Desktop nav */}
        <div className="hidden md:flex items-center gap-2">
          {user && (
            <>
              <span className="text-sm text-muted-foreground mr-2">{user.name}</span>
              <Button variant="ghost" size="sm" asChild>
                <Link to="/settings">
                  <Settings className="h-4 w-4 mr-1" />
                  {t("nav.settings")}
                </Link>
              </Button>
              <Button variant="ghost" size="sm" onClick={handleLogout}>
                <LogOut className="h-4 w-4 mr-1" />
                {t("nav.logout")}
              </Button>
            </>
          )}
        </div>

        {/* Mobile menu */}
        <div className="md:hidden">
          <Button variant="ghost" size="icon" onClick={() => setMenuOpen(!menuOpen)}>
            <Menu className="h-5 w-5" />
          </Button>
        </div>
      </div>

      {/* Mobile dropdown */}
      {menuOpen && user && (
        <div className="md:hidden border-t px-4 py-3 flex flex-col gap-2 bg-background">
          <Link
            to="/settings"
            className="flex items-center gap-2 text-sm py-2"
            onClick={() => setMenuOpen(false)}
          >
            <Settings className="h-4 w-4" />
            {t("nav.settings")}
          </Link>
          <button
            className="flex items-center gap-2 text-sm py-2 text-left"
            onClick={handleLogout}
          >
            <LogOut className="h-4 w-4" />
            {t("nav.logout")}
          </button>
        </div>
      )}
    </nav>
  );
}
