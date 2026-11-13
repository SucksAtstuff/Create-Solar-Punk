"""Builds the Create: Solarpunk wiki website from a checkout of the GitHub wiki.

Usage: python wiki/build.py <wiki checkout> <output dir>

- Home.md becomes the landing page (hero, one card per sidebar section, then the
  rest of Home); every other page becomes <Page>.html
- _Sidebar.md is the navigation: bold lines are sections, [[links]] are pages
- [[Page]] / [[Text|Page]] become links; a link to a missing page fails the build
- Screenshots linked from docs/photos are resized into img/ (recipes are copied)
- theme/ holds the page template, stylesheet, script and icon

Preview: python -m http.server -d <output dir>
"""
import html
import json
import re
import shutil
import sys
from pathlib import Path

import markdown
from PIL import Image

REPO = "SucksAtstuff/Create-Solar-Punk"
SITE_NAME = "Create: Solarpunk"

# Screenshot for each sidebar section's card on the landing page, and for the hero.
HERO_IMAGE = "nuclear_fusion_reactor.png"
SECTION_IMAGES = {
    "Solar Panels": "solar_glass_and_shingles.png",
    "Electricity": "wires_and_connectors.png",
    "Solar Heat": "max_solar_power_tower.png",
    "Steam": "mixed_turbine_blades.png",
    "Biofuel": "sprinkler_fertiziler.png",
    "Geothermal": "geyser.png",
    "Endgame": "nuclear_fusion_core.png",
    "Global Warming": "ash_deco_blocks.png",
    "Building": "rammed_earth_and_chiseled.png",
}
# Card text for sections Home doesn't describe with a "**Name** - text" line.
SECTION_BLURBS = {
    "Endgame": "Mine Lithium and Beryllium, then build a Fusion Reactor and a Particle Accelerator for huge amounts of Steam.",
    "Building": "Salt, Ash, Rammed Earth, Charred Timber, Trellises, Filigree Railings and more.",
    "Global Warming": "An optional pollution system that's off by default. Fuel-burning machines slowly kill the land around them.",
    "More": "Advancements, Display Links and the optional mods Solarpunk works with.",
}
# Sidebar sections that aren't shown as cards (the hero already covers them).
SKIP_CARDS = {"Create: Solarpunk"}

HERE = Path(__file__).parent
THEME = HERE / "theme"
PHOTOS = HERE.parent / "docs" / "photos"
PHOTO_URL = re.compile(r"https://raw\.githubusercontent\.com/" + re.escape(REPO) + r"/[^/]+/docs/photos/([\w/.-]+\.png)")


def main():
    wiki, out = Path(sys.argv[1]), Path(sys.argv[2])
    shutil.rmtree(out, ignore_errors=True)
    (out / "img" / "thumb").mkdir(parents=True)
    for f in ("style.css", "site.js", "icon.png"):
        shutil.copy(THEME / f, out / f)
    (out / ".nojekyll").write_text("")

    sources = {p.stem: p.read_text(encoding="utf-8") for p in wiki.glob("*.md")}
    pages = {k for k in sources if not k.startswith("_")}
    site = Site(pages, out)

    sections = site.parse_sidebar(sources["_Sidebar"])
    order = [slug for _, items in sections for _, slug in items]
    order += sorted(pages - set(order))  # pages missing from the sidebar still get built
    template = (THEME / "page.html").read_text(encoding="utf-8")
    search = []

    for i, slug in enumerate(order):
        md, title = site.prepare(sources[slug])
        body, toc = site.render(md)
        section = next((name for name, items in sections if any(s == slug for _, s in items)), "")
        search.append({"t": title, "u": site.url(slug), "s": section,
                       "h": re.findall(r"<h[23][^>]*>(.*?)<", body),
                       "x": search_text(body).split()})
        if slug == "Home":
            content = site.home(sources["Home"], sections)
        else:
            prev_link = order[i - 1] if i > 0 else None
            next_link = order[i + 1] if i + 1 < len(order) else None
            content = site.article(slug, title, section, body, prev_link, next_link)
        page = (template
                .replace("{{title}}", html.escape(SITE_NAME + " Wiki" if slug == "Home" else f"{title} · {SITE_NAME} Wiki"))
                .replace("{{description}}", html.escape(site.description(md)))
                .replace("{{body_class}}", "home" if slug == "Home" else "page")
                .replace("{{nav}}", site.nav(sections, slug))
                .replace("{{toc}}", "" if slug == "Home" else toc)
                .replace("{{repo}}", REPO)
                .replace("{{content}}", content))
        (out / site.url(slug)).write_text(page, encoding="utf-8")

    for entry in search:
        entry["x"] = " ".join(entry["x"])
    (out / "search.json").write_text(json.dumps(search, separators=(",", ":")), encoding="utf-8")
    not_found = (template.replace("{{title}}", f"Page not found · {SITE_NAME} Wiki")
                 .replace("{{description}}", "").replace("{{body_class}}", "page")
                 .replace("{{nav}}", site.nav(sections, None)).replace("{{toc}}", "").replace("{{repo}}", REPO)
                 .replace("{{content}}", '<article class="prose"><h1>Page not found</h1><p>That page doesn\'t exist. '
                          'Try the search above, or go back to the <a href="index.html">home page</a>.</p></article>'))
    (out / "404.html").write_text(not_found, encoding="utf-8")
    print(f"Built {len(order)} pages into {out}")


class Site:
    def __init__(self, pages, out):
        self.pages, self.out, self.images = pages, out, {}

    # ---- links and markdown ----

    def url(self, slug):
        return "index.html" if slug == "Home" else slug + ".html"

    def target(self, name):
        name, _, anchor = name.partition("#")
        slug = name.strip().replace(" ", "-")
        if slug not in self.pages:
            sys.exit(f"Broken wiki link: [[{name}]]")
        return self.url(slug) + ("#" + slugify(anchor) if anchor else "")

    def link(self, m):
        text, _, page = m.group(1).partition("|")
        return f"[{text}]({self.target(page or text)})"

    def prepare(self, src):
        """Wiki markdown -> plain markdown with local links and images, plus the H1 title."""
        src = re.sub(r"\[\[([^\]]+)\]\]", self.link, src)
        src = re.sub(r"https://github\.com/" + re.escape(REPO) + r"/wiki/([\w-]+)",
                     lambda m: self.url(m.group(1)) if m.group(1) in self.pages else m.group(0), src)
        src = PHOTO_URL.sub(lambda m: self.photo(m.group(1)), src)
        src = re.sub(r"(?m)^ {2,3}([-*]|\d+\.) ", r"    \1 ", src)  # Python-Markdown needs 4-space nesting
        # GitHub starts a list right under a line of text; Python-Markdown needs a blank line first
        src = re.sub(r"(?m)^((?![ \t]*(?:[-*]|\d+\.) |[ \t]*$|[ \t]*\|).+)\n(?=(?:[-*]|\d+\.) )", "\\1\n\n", src)
        title = re.search(r"(?m)^# (.+)$", src)
        return src, title.group(1).strip() if title else ""

    def render(self, md_src):
        md = markdown.Markdown(extensions=["tables", "sane_lists", "attr_list", "fenced_code",
                                           "toc"], extension_configs={"toc": {"slugify": lambda v, s: slugify(v), "toc_depth": "2-3"}})
        body = md.convert(md_src)
        body = re.sub(r"<table>", '<div class="table-wrap"><table>', body).replace("</table>", "</table></div>")
        body = re.sub(r'<img alt="([^"]*)" src="(img/(?!recipes/)[^"]+)"',
                      r'<img class="shot" loading="lazy" alt="\1" src="\2"', body)
        body = re.sub(r'<img alt="([^"]*)" src="(img/recipes/[^"]+)"',
                      r'<img class="recipe" loading="lazy" alt="\1" src="\2"', body)
        body = re.sub(r"(<h([23]) id=\"([^\"]+)\">)(.*?)(</h\2>)",
                      r'\1\4<a class="anchor" href="#\3" aria-label="Link to this section">#</a>\5', body)
        toc = "".join(f'<a class="lvl{t["level"]}" href="#{t["id"]}">{t["name"]}</a>' for t in flatten(md.toc_tokens))
        return body, (f'<p class="toc-title">On this page</p>{toc}' if toc else "")

    def description(self, md_src):
        for para in md_src.split("\n\n"):
            p = para.strip()
            if p and not p.startswith(("#", "!", "|", "-", "*", "<")):
                text = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", p).replace("**", "")
                return text[:200]
        return ""

    # ---- images ----

    def photo(self, rel):
        """Copy or resize a docs/photos image into img/ and return its site path."""
        if rel in self.images:
            return self.images[rel]
        src = PHOTOS / rel
        if not src.exists():
            print(f"warning: missing photo {rel}")
            return f"https://raw.githubusercontent.com/{REPO}/master/docs/photos/{rel}"
        if rel.startswith("recipes/"):
            dst = self.out / "img" / rel
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy(src, dst)
            path = "img/" + rel
        else:
            name = Path(rel).stem + ".jpg"
            im = Image.open(src).convert("RGB")
            for width, folder in ((1600, "img"), (720, "img/thumb")):
                copy = im.copy()
                copy.thumbnail((width, width))
                copy.save(self.out / folder / name, quality=82, optimize=True, progressive=True)
            path = "img/" + name
        self.images[rel] = path
        return path

    def thumb(self, rel):
        return self.photo(rel).replace("img/", "img/thumb/", 1)

    # ---- page pieces ----

    def parse_sidebar(self, src):
        sections = []
        for line in src.splitlines():
            line = line.strip()
            if m := re.fullmatch(r"\*\*(.+)\*\*", line):
                sections.append((m.group(1), []))
            elif m := re.fullmatch(r"- \[\[(.+)\]\]", line):
                text, _, page = m.group(1).partition("|")
                slug = (page or text).strip().replace(" ", "-")
                self.target(page or text)
                if not sections:
                    sections.append(("", []))
                sections[-1][1].append((text.strip(), slug))
        return sections

    def nav(self, sections, current):
        parts = []
        for name, items in sections:
            active = ' class="active" aria-current="page"'
            links = "".join(f'<a href="{self.url(slug)}"{active if slug == current else ""}>{html.escape(text)}</a>'
                            for text, slug in items)
            parts.append(f'<div class="nav-section"><p>{html.escape(name)}</p>{links}</div>')
        return "".join(parts)

    def article(self, slug, title, section, body, prev_slug, next_slug):
        crumbs = f'<p class="eyebrow">{html.escape(section)}</p>' if section else ""
        pager = '<nav class="pager">'
        if prev_slug:
            pager += f'<a class="prev" href="{self.url(prev_slug)}"><span>Previous</span>{html.escape(prev_slug.replace("-", " "))}</a>'
        if next_slug:
            pager += f'<a class="next" href="{self.url(next_slug)}"><span>Next</span>{html.escape(next_slug.replace("-", " "))}</a>'
        pager += "</nav>"
        edit = (f'<p class="edit"><a href="https://github.com/{REPO}/wiki/{slug}/_edit">'
                f'Edit this page on GitHub</a></p>')
        return f'<article class="prose">{crumbs}{body}{edit}</article>{pager}'

    def home(self, src, sections):
        md, title = self.prepare(src)
        # The H1 and first paragraph go in the hero; the rest renders below the cards.
        md = re.sub(r"(?m)^# .+\n+", "", md, count=1)
        intro, _, rest = md.strip().partition("\n\n")
        intro_html = markdown.markdown(intro)
        body, _ = self.render(rest)
        blurbs = {m.group(1).lower(): m.group(2) for m in
                  re.finditer(r"(?m)^\*\*(.+?)\*\* - (.+)$", src)}

        hero_img = self.photo(HERO_IMAGE)
        cards = []
        for name, items in sections:
            if name in SKIP_CARDS or not items:
                continue
            blurb = SECTION_BLURBS.get(name) or blurbs.get(name.lower(), "")
            blurb = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", blurb)
            blurb = blurb[:1].upper() + blurb[1:]
            img = SECTION_IMAGES.get(name)
            figure = (f'<img loading="lazy" alt="" src="{self.thumb(img)}">' if img
                      else '<div class="card-art" aria-hidden="true"></div>')
            links = "".join(f'<li><a href="{self.url(slug)}">{html.escape(text)}</a></li>' for text, slug in items)
            cards.append(f'<section class="card"><a class="card-media" href="{self.url(items[0][1])}" tabindex="-1">{figure}</a>'
                         f'<div class="card-body"><h3><a href="{self.url(items[0][1])}">{html.escape(name)}</a></h3>'
                         f'<p>{blurb}</p><ul>{links}</ul></div></section>')

        guide = '<a class="btn primary" href="Progression-Guide.html">Start the Progression Guide</a>' if "Progression-Guide" in self.pages else ""
        return f"""
<section class="hero" style="--hero: url('{hero_img}')">
  <div class="hero-inner">
    <img class="hero-icon" src="icon.png" alt="" width="88" height="88">
    <p class="eyebrow light">Minecraft 1.21.1 · NeoForge · Create 6</p>
    <h1>{html.escape(title)}</h1>
    <div class="hero-lede">{intro_html}</div>
    <div class="hero-actions">{guide}<a class="btn" href="#chains">Browse the machines</a></div>
  </div>
</section>
<section class="chains" id="chains">
  <div class="chains-head"><p class="eyebrow">Pick your power</p><h2>Every way to run your base</h2></div>
  <div class="cards">{"".join(cards)}</div>
</section>
<article class="prose home-prose">{body}</article>"""


def search_text(body):
    body = re.sub(r"<h1[^>]*>.*?</h1>|<a class=\"anchor\"[^>]*>#</a>", "", body)
    body = re.sub(r"</(p|li|h\d|td|th|tr)>", " ", body)
    return html.unescape(re.sub(r"<[^>]+>", "", body))


def slugify(value):
    value = re.sub(r"<[^>]+>", "", value).strip().lower()
    value = re.sub(r"[^\w\- ]", "", value)
    return re.sub(r"\s", "-", value)


def flatten(tokens):
    for t in tokens:
        yield t
        yield from flatten(t["children"])


if __name__ == "__main__":
    main()
