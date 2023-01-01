/*
 * Copyright © 2021-2022 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hud.snapping;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import io.github.axolotlclient.util.Util;
import lombok.Setter;
import net.minecraft.client.util.Window;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 * @license GPL-3.0
 */

public class SnappingHelper {

    private final int distance = 4;
    private final HashSet<Integer> x = new HashSet<>();
    private final HashSet<Integer> y = new HashSet<>();
    private static final Color LINE_COLOR = Color.SELECTOR_BLUE;
    @Setter
    private Rectangle current;
    private final Window window;

    public SnappingHelper(List<Rectangle> rects, Rectangle current) {
        addAllRects(rects);
        this.current = current;
        this.window = Util.getWindow();
    }

    public static Optional<Integer> getNearby(int pos, HashSet<Integer> set, int distance) {
        for (Integer integer : set) {
            if (integer - distance <= pos && integer + distance >= pos) {
                return Optional.of(integer);
            }
        }
        return Optional.empty();
    }

    public void addAllRects(List<Rectangle> rects) {
        for (Rectangle rect : rects) {
            addRect(rect);
        }
    }

    public void addRect(Rectangle rect) {
        x.add(rect.x);
        x.add(rect.x + rect.width);
        y.add(rect.y);
        y.add(rect.y + rect.height);
    }

    public void renderSnaps() {
        Integer curx, cury;
        if ((curx = getRawXSnap()) != null) {
            DrawUtil.fillRect(new Rectangle(curx, 0, 1, (int) window.getScaledHeight()), LINE_COLOR);
        }
        if ((cury = getRawYSnap()) != null) {
            DrawUtil.fillRect(new Rectangle(0, cury, (int) window.getScaledWidth(), 1), LINE_COLOR);
        }
        //renderAll();
    }

    public void renderAll() {
        for (Integer xval : x) {
            DrawUtil.fillRect(new Rectangle(xval, 0, 1, (int) window.getScaledHeight()), Color.WHITE);
        }
        for (Integer yval : y) {
            DrawUtil.fillRect(new Rectangle(0, yval, (int) window.getScaledWidth(), 1), Color.WHITE);
        }
    }

    public Integer getCurrentXSnap() {
        Integer xSnap = getNearby(current.x, x, distance).orElse(null);
        if (xSnap != null) {
            return xSnap;
        } else if ((xSnap = getNearby(current.x + current.width, x, distance).orElse(null)) != null) {
            return xSnap - current.width;
        } else if ((xSnap = getHalfXSnap()) != null) {
            return xSnap - (current.width / 2);
        }
        return null;
    }

    public Integer getRawXSnap() {
        Integer xSnap = getNearby(current.x, x, distance).orElse(null);
        if (xSnap != null) {
            return xSnap;
        } else if ((xSnap = getNearby(current.x + current.width, x, distance).orElse(null)) != null) {
            return xSnap;
        } else if ((xSnap = getHalfXSnap()) != null) {
            return xSnap;
        }
        return null;
    }

    public Integer getCurrentYSnap() {
        Integer ySnap = getNearby(current.y, y, distance).orElse(null);
        if (ySnap != null) {
            return ySnap;
        } else if ((ySnap = getNearby(current.y + current.height, y, distance).orElse(null)) != null) {
            return ySnap - current.height;
        } else if ((ySnap = getHalfYSnap()) != null) {
            return ySnap - (current.height / 2);
        }
        return null;
    }

    public Integer getHalfYSnap() {
        int height = (int) window.getScaledHeight() / 2;
        int pos = current.y + Math.round((float) current.height / 2);
        if (height - distance <= pos && height + distance >= pos) {
            return height;
        }
        return null;
    }

    public Integer getHalfXSnap() {
        int width = (int) window.getScaledWidth() / 2;
        int pos = current.x + Math.round((float) current.width / 2);
        if (width - distance <= pos && width + distance >= pos) {
            return width;
        }
        return null;
    }

    public Integer getRawYSnap() {
        Integer ySnap = getNearby(current.y, y, distance).orElse(null);
        if (ySnap != null) {
            return ySnap;
        } else if ((ySnap = getNearby(current.y + current.height, y, distance).orElse(null)) != null) {
            return ySnap;
        } else if ((ySnap = getHalfYSnap()) != null) {
            return ySnap;
        }
        return null;
    }
}
