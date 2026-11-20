// Theme toggle, navigation drawer, search, screenshot lightbox and "on this page" highlighting.
(function () {
  const root = document.documentElement;
  const body = document.body;

  // ---- theme ----
  const themeBtn = document.querySelector(".theme-btn");
  themeBtn.addEventListener("click", () => {
    const dark = root.dataset.theme
      ? root.dataset.theme === "dark"
      : matchMedia("(prefers-color-scheme: dark)").matches;
    root.dataset.theme = dark ? "light" : "dark";
    try { localStorage.setItem("theme", root.dataset.theme); } catch (e) {}
  });

  // ---- navigation drawer ----
  const menuBtn = document.querySelector(".menu-btn");
  const scrim = document.querySelector(".scrim");
  function setNav(open) {
    body.classList.toggle("nav-open", open);
    menuBtn.setAttribute("aria-expanded", String(open));
    scrim.hidden = !open;
  }
  menuBtn.addEventListener("click", () => setNav(!body.classList.contains("nav-open")));
  scrim.addEventListener("click", () => setNav(false));
  document.addEventListener("keydown", (e) => { if (e.key === "Escape") setNav(false); });
  const active = document.querySelector(".sidebar a.active");
  if (active) active.scrollIntoView({ block: "center" });

  // ---- search ----
  const input = document.querySelector(".search input");
  const box = document.querySelector(".results");
  let index = null;
  let selected = -1;

  function load() {
    if (index) return Promise.resolve(index);
    return fetch("search.json").then((r) => r.json()).then((data) => {
      index = data.map((d) => ({ ...d, tl: d.t.toLowerCase(), hl: d.h.join(" ").toLowerCase(), xl: d.x.toLowerCase() }));
      return index;
    });
  }

  function escapeHtml(s) {
    return s.replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
  }

  function snippet(doc, terms) {
    const at = terms.map((t) => doc.xl.indexOf(t)).filter((i) => i >= 0).sort((a, b) => a - b)[0];
    if (at === undefined) return "";
    const start = Math.max(0, at - 50);
    let text = (start > 0 ? "…" : "") + doc.x.slice(start, at + 110) + "…";
    text = escapeHtml(text);
    for (const t of terms) {
      text = text.replace(new RegExp("(" + t.replace(/[.*+?^${}()|[\]\\]/g, "\\$&") + ")", "ig"), "<mark>$1</mark>");
    }
    return text;
  }

  function search(q) {
    const terms = q.toLowerCase().split(/\s+/).filter(Boolean);
    if (!terms.length) return [];
    const hits = [];
    for (const doc of index) {
      let score = 0;
      for (const t of terms) {
        let s = 0;
        if (doc.tl === t) s += 40;
        if (doc.tl.includes(t)) s += 20;
        if (doc.hl.includes(t)) s += 8;
        const n = doc.xl.split(t).length - 1;
        if (n) s += Math.min(n, 10);
        if (!s) { score = 0; break; } // every word has to match somewhere
        score += s;
      }
      if (score) hits.push({ doc, score });
    }
    return hits.sort((a, b) => b.score - a.score).slice(0, 8).map((h) => ({ ...h, terms }));
  }

  function show(q) {
    const hits = search(q);
    selected = hits.length ? 0 : -1;
    if (!q.trim()) { box.hidden = true; return; }
    box.innerHTML = hits.length
      ? hits.map((h, i) =>
          `<a role="option" href="${h.doc.u}" aria-selected="${i === 0}">` +
          (h.doc.s ? `<span class="where">${escapeHtml(h.doc.s)}</span>` : "") +
          `<strong>${escapeHtml(h.doc.t)}</strong><span class="snippet">${snippet(h.doc, h.terms)}</span></a>`).join("")
      : `<div class="empty">No pages match “${escapeHtml(q)}”.</div>`;
    box.hidden = false;
  }

  function move(step) {
    const items = [...box.querySelectorAll("a")];
    if (!items.length) return;
    selected = (selected + step + items.length) % items.length;
    items.forEach((a, i) => a.setAttribute("aria-selected", String(i === selected)));
    items[selected].scrollIntoView({ block: "nearest" });
  }

  input.addEventListener("focus", () => { load().then(() => { if (input.value) show(input.value); }); });
  input.addEventListener("input", () => load().then(() => show(input.value)));
  input.addEventListener("keydown", (e) => {
    if (e.key === "ArrowDown") { e.preventDefault(); move(1); }
    else if (e.key === "ArrowUp") { e.preventDefault(); move(-1); }
    else if (e.key === "Enter") {
      const a = box.querySelectorAll("a")[selected];
      if (a) location.href = a.href;
    } else if (e.key === "Escape") { input.value = ""; box.hidden = true; input.blur(); }
  });
  document.addEventListener("click", (e) => { if (!e.target.closest(".search")) box.hidden = true; });
  document.addEventListener("keydown", (e) => {
    if (e.key === "/" && !/^(INPUT|TEXTAREA)$/.test(document.activeElement.tagName)) {
      e.preventDefault();
      input.focus();
    }
  });

  // ---- lightbox for screenshots ----
  const dialog = document.querySelector(".lightbox");
  if (dialog && dialog.showModal) {
    const big = dialog.querySelector("img");
    document.querySelectorAll(".prose img.shot").forEach((img) => {
      img.addEventListener("click", () => {
        big.src = img.src;
        big.alt = img.alt;
        dialog.showModal();
      });
    });
    dialog.addEventListener("click", () => dialog.close());
  }

  // ---- highlight the current section in "On this page" ----
  const tocLinks = [...document.querySelectorAll(".toc a")];
  if (tocLinks.length && "IntersectionObserver" in window) {
    const byId = new Map(tocLinks.map((a) => [decodeURIComponent(a.hash.slice(1)), a]));
    const headings = [...byId.keys()].map((id) => document.getElementById(id)).filter(Boolean);
    const visible = new Set();
    const observer = new IntersectionObserver((entries) => {
      for (const e of entries) e.isIntersecting ? visible.add(e.target) : visible.delete(e.target);
      let current = headings.filter((h) => visible.has(h))[0];
      if (!current) current = headings.filter((h) => h.getBoundingClientRect().top < 120).pop();
      tocLinks.forEach((a) => a.classList.toggle("current", current && a.hash === "#" + current.id));
    }, { rootMargin: "-60px 0px -60% 0px" });
    headings.forEach((h) => observer.observe(h));
  }
})();
