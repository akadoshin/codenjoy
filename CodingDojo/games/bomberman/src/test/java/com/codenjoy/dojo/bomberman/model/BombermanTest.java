package com.codenjoy.dojo.bomberman.model;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.codenjoy.dojo.bomberman.model.perks.BombBlastRadiusIncrease;
import com.codenjoy.dojo.bomberman.model.perks.BombCountIncrease;
import com.codenjoy.dojo.bomberman.model.perks.BombImmune;
import com.codenjoy.dojo.bomberman.model.perks.PerksSettingsWrapper;
import com.codenjoy.dojo.bomberman.services.DefaultGameSettings;
import com.codenjoy.dojo.bomberman.services.Events;
import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.services.multiplayer.Single;
import com.codenjoy.dojo.services.printer.PrinterFactory;
import com.codenjoy.dojo.services.printer.PrinterFactoryImpl;
import com.codenjoy.dojo.services.round.RoundSettingsWrapper;
import com.codenjoy.dojo.services.settings.SimpleParameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.codenjoy.dojo.services.settings.SimpleParameter.v;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * User: oleksandr.baglai
 * Date: 3/7/13
 * Time: 9:07 AM
 */
public class BombermanTest {

    public int SIZE = 5;
    private Game game;
    private Joystick hero;
    private Level level;
    private WallsImpl walls;
    private GameSettings settings;
    private EventListener listener;
    private Dice meatChppperDice;
    private Dice bombermanDice;
    private Player player;
    private List bombermans;
    private Bomberman field;
    private final PrinterFactory printer = new PrinterFactoryImpl();

    @Before
    public void setUp() {
        meatChppperDice = mock(Dice.class);
        bombermanDice = mock(Dice.class);

        level = mock(Level.class);
        canDropBombs(1);
        bombsPower(1);
        walls = mock(WallsImpl.class);
        when(walls.iterator()).thenReturn(Collections.emptyIterator());
        settings = mock(GameSettings.class);
        listener = mock(EventListener.class);

        when(settings.getWalls(any(Bomberman.class))).thenReturn(walls);
        when(settings.getLevel()).thenReturn(level);
        when(settings.getRoundSettings()).thenReturn(getRoundSettings());
        when(settings.killOtherBombermanScore()).thenReturn(v(200));
        when(settings.killMeatChopperScore()).thenReturn(v(100));
        when(settings.killWallScore()).thenReturn(v(10));

        initBomberman();
        givenBoard(SIZE);
        PerksSettingsWrapper.clear();
    }

    private void initBomberman() {
        dice(bombermanDice, 0, 0);
        Hero hero = new Hero(level, bombermanDice);
        when(settings.getBomberman(level)).thenReturn(hero);
        this.hero = hero;
    }

    public static RoundSettingsWrapper getRoundSettings() {
        return new DefaultGameSettings(mock(Dice.class)).getRoundSettings();
    }

    private void givenBoard(int size) {
        when(settings.getBoardSize()).thenReturn(v(size));
        field = new Bomberman(settings);
        player = new Player(listener, getRoundSettings().roundsEnabled());
        game = new Single(player, printer);
        game.on(field);
        dice(bombermanDice, 0, 0);
        game.newGame();
        hero = game.getJoystick();
    }

    private SimpleParameter<Boolean> getRoundsEnabled() {
        return new SimpleParameter<>(false);
    }

    @Test
    public void shouldBoard_whenStartGame() {
        when(settings.getBoardSize()).thenReturn(v(10));

        Bomberman board = new Bomberman(settings);

        assertEquals(10, board.size());
    }

    @Test
    public void shouldBoard_whenStartGame2() {
        assertEquals(SIZE, field.size());
    }

    @Test
    public void shouldBombermanOnBoardAtInitPos_whenGameStart() {
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺    \n");
    }

    @Test
    public void shouldBombermanOnBoardOneRightStep_whenCallRightCommand() {
        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " ☺   \n");
    }

    @Test
    public void shouldBombermanOnBoardTwoRightSteps_whenCallRightCommandTwice() {
        hero.right();
        field.tick();

        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "  ☺  \n");
    }

    @Test
    public void shouldBombermanOnBoardOneUpStep_whenCallDownCommand() {
        hero.up();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "☺    \n" +
                "     \n");
    }

    @Test
    public void shouldBombermanWalkUp() {
        hero.up();
        field.tick();

        hero.up();
        field.tick();

        hero.down();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "☺    \n" +
                "     \n");
    }

    @Test
    public void shouldBombermanStop_whenGoToWallDown() {
        hero.down();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺    \n");
    }

    private void assertBombermanAt(int x, int y) {
        assertEquals(x, player.getHero().getX());
        assertEquals(y, player.getHero().getY());
    }

    @Test
    public void shouldBombermanWalkLeft() {
        hero.right();
        field.tick();

        hero.right();
        field.tick();

        hero.left();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " ☺   \n");
    }

    @Test
    public void shouldBombermanStop_whenGoToWallLeft() {
        hero.left();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺    \n");
    }

    @Test
    public void shouldBombermanStop_whenGoToWallRight() {
        gotoMaxRight();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "    ☺\n");
    }

    @Test
    public void shouldBombermanStop_whenGoToWallUp() {
        gotoMaxUp();

        asrtBrd("☺    \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "     \n");
    }

    private void gotoMaxUp() {
        for (int y = 0; y <= SIZE + 1; y++) {
            hero.up();
            field.tick();
        }
    }

    @Test
    public void shouldBombermanMovedOncePerTact() {
        hero.down();
        hero.up();
        hero.left();
        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " ☺   \n");

        hero.right();
        hero.left();
        hero.down();
        hero.up();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " ☺   \n" +
                "     \n");
    }

    @Test
    public void shouldBombDropped_whenBombermanDropBomb() {
        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☻    \n");
    }

    @Test
    public void shouldBombDropped_whenBombermanDropBombAtAnotherPlace() {
        hero.up();
        field.tick();

        hero.right();
        field.tick();

        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " ☻   \n" +
                "     \n");
    }

    @Test
    public void shouldBombsDropped_whenBombermanDropThreeBomb() {
        canDropBombs(3);

        hero.up();
        field.tick();
        hero.act();
        field.tick();

        hero.right();
        field.tick();
        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "2☻   \n" +
                "     \n");
    }

    private void canDropBombs(int countBombs) {
        reset(level);
        when(level.bombsCount()).thenReturn(countBombs);
    }

    // проверить, что бомбермен не может бомб дропать больше, чем у него в level прописано
    @Test
    public void shouldOnlyTwoBombs_whenLevelApproveIt() {
        canDropBombs(2);

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺    \n");

        hero.up();
        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "☻    \n" +
                "     \n");

        hero.up();
        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "☻    \n" +
                "3    \n" +
                "     \n");

        hero.up();
        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "☺    \n" +
                "3    \n" +
                "2    \n" +
                "     \n");
    }

    // бомберен не может дропать два бомбы на одно место
    @Test
    public void shouldOnlyOneBombPerPlace() {
        canDropBombs(2);

        hero.act();
        field.tick();

        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☻    \n");

        assertEquals(1, field.getBombs().size());

        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "2☺   \n");

        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "1 ☺  \n");

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "҉ ☺  \n");

        field.tick();   // бомб больше нет, иначе тут был бы взрыв второй

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "  ☺  \n");
    }

    @Test
    public void shouldBoom_whenDroppedBombHas5Ticks() {
        hero.act();
        field.tick();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "1 ☺  \n");

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "҉    \n" +
                "҉҉☺  \n");
    }

    // проверить, что я могу поставить еще одну бомбу, когда другая рванула
    @Test
    public void shouldCanDropNewBomb_whenOtherBoom() {
        shouldBoom_whenDroppedBombHas5Ticks();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "҉    \n" +
                "҉҉☺  \n");

        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "  ☻  \n");
    }

    // если бомбермен стоит на бомбе то он умирает после ее взрыва
    @Test
    public void shouldKillBoomberman_whenBombExploded() {
        hero.act();
        hero.right();
        field.tick();
        field.tick();
        field.tick();

        field.tick();

        assertBombermanAlive();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "1☺   \n");

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "҉    \n" +
                "҉Ѡ   \n");
        assertBombermanDie();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " Ѡ   \n");
        assertBombermanDie();
    }

    private void assertBombermanDie() {
        assertTrue("Expected model over", game.isGameOver());
    }

    // после смерти ходить больше нельзя
    @Test
    public void shouldException_whenTryToMoveIfDead_goLeft() {
        killBomber();

        hero.left();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " Ѡ   \n" +
                "     \n");
    }

    private void killBomber() {
        hero.up();
        field.tick();
        hero.right();
        hero.act();
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                " ҉   \n" +
                "҉Ѡ҉  \n" +
                " ҉   \n");
    }

    @Test
    public void shouldException_whenTryToMoveIfDead_goUp() {
        killBomber();

        hero.up();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " Ѡ   \n" +
                "     \n");
    }

    @Test
    public void shouldException_whenTryToMoveIfDead_goDown() {
        killBomber();

        hero.down();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " Ѡ   \n" +
                "     \n");
    }

    @Test
    public void shouldException_whenTryToMoveIfDead_goRight() {
        killBomber();

        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " Ѡ   \n" +
                "     \n");
    }

    @Test
    public void shouldException_whenTryToMoveIfDead_dropBomb() {
        killBomber();

        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " Ѡ   \n" +
                "     \n");
    }

    // если бомбермен стоит под действием ударной волны, он умирает
    @Test
    public void shouldKillBoomberman_whenBombExploded_blastWaveAffect_fromLeft() {
        hero.act();
        field.tick();
        hero.right();
        field.tick();
        field.tick();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "1☺   \n");
        assertBombermanAlive();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "҉    \n" +
                "҉Ѡ   \n");
        assertBombermanDie();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " Ѡ   \n");
        assertBombermanDie();
    }

    @Test
    public void shouldKillBoomberman_whenBombExploded_blastWaveAffect_fromRight() {
        hero.right();
        field.tick();
        hero.act();
        field.tick();
        hero.left();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺1   \n");
        assertBombermanAlive();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " ҉   \n" +
                "Ѡ҉҉  \n");
        assertBombermanDie();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "Ѡ    \n");
        assertBombermanDie();
    }

    @Test
    public void shouldKillBoomberman_whenBombExploded_blastWaveAffect_fromUp() {
        hero.up();
        hero.act();
        field.tick();
        hero.down();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "1    \n" +
                "☺    \n");
        assertBombermanAlive();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "҉    \n" +
                "҉҉   \n" +
                "Ѡ    \n");
        assertBombermanDie();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "Ѡ    \n");
        assertBombermanDie();
    }

    private void assertBombermanAlive() {
        assertFalse(game.isGameOver());
    }

    @Test
    public void shouldKillBoomberman_whenBombExploded_blastWaveAffect_fromDown() {
        hero.down();
        field.tick();
        hero.act();
        field.tick();
        hero.up();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "☺    \n" +
                "1    \n");
        assertBombermanAlive();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "Ѡ    \n" +
                "҉҉   \n");
        assertBombermanDie();

        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "Ѡ    \n" +
                "     \n");
        assertBombermanDie();
    }

    @Test
    public void shouldNoKillBoomberman_whenBombExploded_blastWaveAffect_fromDownRight() {
        hero.down();
        field.tick();
        hero.right();
        field.tick();
        hero.act();
        field.tick();
        hero.up();
        field.tick();
        hero.left();
        field.tick();

        field.tick();

        assertBombermanAlive();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "☺    \n" +
                " 1   \n");

        field.tick();

        assertBombermanAlive();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "☺҉   \n" +
                "҉҉҉  \n");
    }

    @Test
    public void shouldSameBoomberman_whenNetFromBoard() {
        assertSame(hero, game.getJoystick());
    }

    @Test
    public void shouldBlastAfter_whenBombExposed() {
        hero.act();
        field.tick();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "҉    \n" +
                "҉҉☺  \n");
    }

    @Test
    public void shouldBlastAfter_whenBombExposed_inOtherCorner() {
        gotoMaxUp();
        gotoMaxRight();

        hero.act();
        field.tick();
        hero.left();
        field.tick();
        hero.left();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("  ☺҉҉\n" +
                "    ҉\n" +
                "     \n" +
                "     \n" +
                "     \n");
    }

    @Test
    public void shouldBlastAfter_whenBombExposed_bombermanDie() {
        gotoBoardCenter();
        hero.act();
        field.tick();
        hero.down();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "  ҉  \n" +
                " ҉҉҉ \n" +
                "  Ѡ  \n" +
                "     \n");

        assertBombermanDie();

        field.tick();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "  Ѡ  \n" +
                "     \n");

        assertBombermanDie();
    }

    private void gotoBoardCenter() {
        for (int y = 0; y < SIZE / 2; y++) {
            hero.up();
            field.tick();
            hero.right();
            field.tick();
        }
    }

    private void asrtBrd(String expected) {
        assertEquals(expected, printer.getPrinter(
                field.reader(), player).print());
    }

    // появляются стенки, которые конфигурятся извне
    @Test
    public void shouldBombermanNotAtWall() {
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☺    \n");

        givenBoardWithWalls();

        asrtBrd("☼☼☼☼☼\n" +
                "☼   ☼\n" +
                "☼ ☼ ☼\n" +
                "☼☺  ☼\n" +
                "☼☼☼☼☼\n");

    }

    // бомбермен не может пойти вперед на стенку
    @Test
    public void shouldBombermanStop_whenUpWall() {
        givenBoardWithWalls();

        hero.down();
        field.tick();

        asrtBrd("☼☼☼☼☼\n" +
                "☼   ☼\n" +
                "☼ ☼ ☼\n" +
                "☼☺  ☼\n" +
                "☼☼☼☼☼\n");
    }

    @Test
    public void shouldBombermanStop_whenLeftWall() {
        givenBoardWithWalls();

        hero.left();
        field.tick();

        asrtBrd("☼☼☼☼☼\n" +
                "☼   ☼\n" +
                "☼ ☼ ☼\n" +
                "☼☺  ☼\n" +
                "☼☼☼☼☼\n");
    }

    @Test
    public void shouldBombermanStop_whenRightWall() {
        givenBoardWithWalls();

        gotoMaxRight();

        asrtBrd("☼☼☼☼☼\n" +
                "☼   ☼\n" +
                "☼ ☼ ☼\n" +
                "☼  ☺☼\n" +
                "☼☼☼☼☼\n");
    }

    @Test
    public void shouldBombermanStop_whenDownWall() {
        givenBoardWithWalls();

        gotoMaxUp();

        asrtBrd("☼☼☼☼☼\n" +
                "☼☺  ☼\n" +
                "☼ ☼ ☼\n" +
                "☼   ☼\n" +
                "☼☼☼☼☼\n");
    }

    private void gotoMaxRight() {
        for (int x = 0; x <= SIZE + 1; x++) {
            hero.right();
            field.tick();
        }
    }

    private void givenBoardWithWalls() {
        givenBoardWithWalls(SIZE);
    }

    private void givenBoardWithWalls(int size) {
        withWalls(new OriginalWalls(v(size)));
        givenBoard(size);
    }

    private void givenBoardWithDestroyWalls() {
        givenBoardWithDestroyWalls(SIZE);
    }

    private void givenBoardWithDestroyWalls(int size) {
        withWalls(new DestroyWalls(new OriginalWalls(v(size))));
        givenBoard(size);
    }

    private void withWalls(Walls walls) {
        when(settings.getWalls(any(Bomberman.class))).thenReturn(walls);
    }

    private void givenBoardWithOriginalWalls() {
        givenBoardWithOriginalWalls(SIZE);
    }

    private void givenBoardWithOriginalWalls(int size) {
        withWalls(new OriginalWalls(v(size)));
        givenBoard(size);
    }

    // бомбермен не может вернуться на место бомбы, она его не пускает как стена
    @Test
    public void shouldBombermanStop_whenGotoBomb() {
        hero.act();
        field.tick();
        hero.right();
        field.tick();

        hero.left();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "2☺   \n");
    }

    // проверить, что бомбермен может одноверменно перемещаться по полю и дропать бомбы за один такт, только как именно?
    @Test
    public void shouldBombermanWalkAndDropBombsTogetherInOneTact_bombFirstly() {
        hero.act();
        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "4☺   \n");
    }

    @Test
    public void shouldBombermanWalkAndDropBombsTogetherInOneTact_moveFirstly() {
        hero.right();
        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " ☻   \n");

        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " 3☺  \n");
    }

    @Test
    public void shouldBombermanWalkAndDropBombsTogetherInOneTact_bombThanMove() {
        hero.act();
        field.tick();
        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "3☺   \n");
    }

    @Test
    public void shouldBombermanWalkAndDropBombsTogetherInOneTact_moveThanBomb() {
        hero.right();
        field.tick();
        hero.act();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " ☻   \n");

        hero.right();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " 3☺  \n");
    }

    @Test
    public void shouldWallProtectsBomberman() {
        givenBoardWithOriginalWalls();

        hero.act();
        goOut();

        asrtBrd("☼☼☼☼☼\n" +
                "☼  ☺☼\n" +
                "☼ ☼ ☼\n" +
                "☼1  ☼\n" +
                "☼☼☼☼☼\n");

        field.tick();

        asrtBrd("☼☼☼☼☼\n" +
                "☼  ☺☼\n" +
                "☼҉☼ ☼\n" +
                "☼҉҉ ☼\n" +
                "☼☼☼☼☼\n");

        assertBombermanAlive();
    }

    @Test
    public void shouldWallProtectsBomberman2() {
        assertBombPower(5,
                "☼☼☼☼☼☼☼☼☼\n" +
                        "☼       ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉      ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉ ☺    ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉҉҉҉҉҉ ☼\n" +
                        "☼☼☼☼☼☼☼☼☼\n");

        assertBombermanAlive();
    }

    private void bombsPower(int power) {
        when(level.bombsPower()).thenReturn(power);
    }

    // проверить, что разрыв бомбы длинной указанной в level
    @Test
    public void shouldChangeBombPower_to2() {
        assertBombPower(2,
                "☼☼☼☼☼☼☼☼☼\n" +
                        "☼       ☼\n" +
                        "☼ ☼ ☼ ☼ ☼\n" +
                        "☼       ☼\n" +
                        "☼ ☼ ☼ ☼ ☼\n" +
                        "☼҉ ☺    ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉҉҉    ☼\n" +
                        "☼☼☼☼☼☼☼☼☼\n");
    }

    @Test
    public void shouldChangeBombPower_to3() {
        assertBombPower(3,
                "☼☼☼☼☼☼☼☼☼\n" +
                        "☼       ☼\n" +
                        "☼ ☼ ☼ ☼ ☼\n" +
                        "☼       ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉ ☺    ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉҉҉҉   ☼\n" +
                        "☼☼☼☼☼☼☼☼☼\n");
    }

    @Test
    public void shouldChangeBombPower_to6() {
        assertBombPower(6,
                "☼☼☼☼☼☼☼☼☼\n" +
                        "☼҉      ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉      ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉ ☺    ☼\n" +
                        "☼҉☼ ☼ ☼ ☼\n" +
                        "☼҉҉҉҉҉҉҉☼\n" +
                        "☼☼☼☼☼☼☼☼☼\n");
    }

    private void assertBombPower(int power, String expected) {
        givenBoardWithOriginalWalls(9);
        bombsPower(power);

        hero.act();
        goOut();
        field.tick();

        asrtBrd(expected);
    }

    private void goOut() {
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        hero.up();
        field.tick();
        hero.up();
        field.tick();
    }

    // я немогу модифицировать список бомб на доске, меняя getBombs
    // но список бомб, что у меня на руках обязательно синхронизирован с теми, что на поле
    @Test
    public void shouldNoChangeOriginalBombsWhenUseBoardApiButTimersSynchronized() {
        canDropBombs(2);
        hero.act();
        hero.right();
        field.tick();
        hero.act();
        hero.right();
        field.tick();

        List<Bomb> bombs1 = field.getBombs();
        List<Bomb> bombs2 = field.getBombs();
        List<Bomb> bombs3 = field.getBombs();
        assertSame(bombs1, bombs2);
        assertSame(bombs2, bombs3);
        assertSame(bombs3, bombs1);

        Bomb bomb11 = bombs1.get(0);
        Bomb bomb12 = bombs2.get(0);
        Bomb bomb13 = bombs3.get(0);
        assertSame(bomb11, bomb12);
        assertSame(bomb12, bomb13);
        assertSame(bomb13, bomb11);

        Bomb bomb21 = bombs1.get(1);
        Bomb bomb22 = bombs2.get(1);
        Bomb bomb23 = bombs3.get(1);
        assertSame(bomb21, bomb22);
        assertSame(bomb22, bomb23);
        assertSame(bomb23, bomb21);

        field.tick();
        field.tick();

        assertFalse(bomb11.isExploded());
        assertFalse(bomb12.isExploded());
        assertFalse(bomb13.isExploded());

        field.tick();

        assertTrue(bomb11.isExploded());
        assertTrue(bomb12.isExploded());
        assertTrue(bomb13.isExploded());

        assertFalse(bomb21.isExploded());
        assertFalse(bomb22.isExploded());
        assertFalse(bomb23.isExploded());

        field.tick();

        assertTrue(bomb21.isExploded());
        assertTrue(bomb22.isExploded());
        assertTrue(bomb23.isExploded());
    }

    @Test
    public void shouldReturnShouldNotSynchronizedBombsList_whenUseBoardApi() {
        hero.act();
        hero.right();
        field.tick();

        List<Bomb> bombs1 = field.getBombs();
        assertEquals(1, bombs1.size());

        field.tick();
        field.tick();
        field.tick();
        field.tick();

        List<Bomb> bombs2 = field.getBombs();
        assertEquals(0, bombs2.size());
        assertEquals(0, bombs1.size());
        assertSame(bombs1, bombs2);
    }

    @Test
    public void shouldChangeBlast_whenUseBoardApi() {  // TODO а нода вообще такое? стреляет по перформансу перекладывать объекты и усложняет код
        hero.act();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        List<Blast> blasts1 = field.getBlasts();
        List<Blast> blasts2 = field.getBlasts();
        List<Blast> blasts3 = field.getBlasts();
        assertSame(blasts1, blasts2);
        assertSame(blasts2, blasts3);
        assertSame(blasts3, blasts1);

        Point blast11 = blasts1.get(0);
        Point blast12 = blasts2.get(0);
        Point blast13 = blasts3.get(0);
        assertSame(blast11, blast12);
        assertSame(blast12, blast13);
        assertSame(blast13, blast11);

        Point blast21 = blasts1.get(1);
        Point blast22 = blasts2.get(1);
        Point blast23 = blasts3.get(1);
        assertSame(blast21, blast22);
        assertSame(blast22, blast23);
        assertSame(blast23, blast21);
    }

    @Test
    public void shouldNoChangeWall_whenUseBoardApi() {
        givenBoardWithWalls();

        Walls walls1 = field.getWalls();
        Walls walls2 = field.getWalls();
        Walls walls3 = field.getWalls();
        assertNotSame(walls1, walls2);
        assertNotSame(walls2, walls3);
        assertNotSame(walls3, walls1);

        Iterator<Wall> iterator1 = walls1.iterator();
        Iterator<Wall> iterator2 = walls2.iterator();
        Iterator<Wall> iterator3 = walls3.iterator();

        Point wall11 = iterator1.next();
        Point wall12 = iterator2.next();
        Point wall13 = iterator3.next();
        assertNotSame(wall11, wall12);
        assertNotSame(wall12, wall13);
        assertNotSame(wall13, wall11);

        Point wall21 = iterator1.next();
        Point wall22 = iterator2.next();
        Point wall23 = iterator3.next();
        assertNotSame(wall21, wall22);
        assertNotSame(wall22, wall23);
        assertNotSame(wall23, wall21);
    }

    // в настройках уровня так же есть и разрущающиеся стены
    @Test
    public void shouldRandomSetDestroyWalls_whenStart() {
        givenBoardWithDestroyWalls();

        asrtBrd("#####\n" +
                "#   #\n" +
                "# # #\n" +
                "#☺  #\n" +
                "#####\n");
    }

    // они взрываются от ударной волны
    @Test
    public void shouldDestroyWallsDestroyed_whenBombExploded() {
        givenBoardWithDestroyWalls();

        hero.act();
        goOut();

        asrtBrd("#####\n" +
                "#  ☺#\n" +
                "# # #\n" +
                "#1  #\n" +
                "#####\n");

        field.tick();

        asrtBrd("#####\n" +
                "#  ☺#\n" +
                "#҉# #\n" +
                "H҉҉ #\n" +
                "#H###\n");
    }

    private void dice(Dice dice, int... values) {
        OngoingStubbing<Integer> when = when(dice.next(anyInt()));
        for (int value : values) {
            when = when.thenReturn(value);
        }
    }

    // появляются чертики, их несоклько за игру
    // каждый такт чертики куда-то рендомно муваются
    // если бомбермен и чертик попали в одну клетку - бомбермен умирает
    @Test
    public void shouldRandomMoveMonster() {
        givenBoardWithMeatChopper(11);
        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼        &☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        dice(meatChppperDice, 1, Direction.DOWN.value());
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼&☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        dice(meatChppperDice, 1);
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼        &☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        dice(meatChppperDice, 0, Direction.LEFT.value());
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼       & ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼&        ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        dice(meatChppperDice, 1, Direction.RIGHT.value());
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼ &       ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        dice(meatChppperDice, 0, Direction.LEFT.value());
        field.tick();
        field.tick();

        dice(meatChppperDice, Direction.LEFT.value());
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼&        ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        dice(meatChppperDice, Direction.DOWN.value());
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼&☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼Ѡ        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        Assert.assertTrue(game.isGameOver());
        verify(listener).event(Events.KILL_BOMBERMAN);
    }

    private void givenBoardWithMeatChopper(int size) {
        dice(meatChppperDice, size - 2, size - 2);

        Field temp = mock(Field.class);
        when(temp.size()).thenReturn(size);
        MeatChoppers walls = new MeatChoppers(new OriginalWalls(v(size)), temp, v(1), meatChppperDice);
        bombermans = mock(List.class);
        when(bombermans.contains(anyObject())).thenReturn(false);
        when(temp.getBombermans()).thenReturn(bombermans);
        withWalls(walls);
        walls.regenerate();
        givenBoard(size);

        dice(meatChppperDice, 1, Direction.UP.value());  // Чертик будет упираться в стенку и стоять на месте
    }

    // чертик умирает, если попадает под взывающуюся бомбу
    @Test
    public void shouldDieMonster_whenBombExploded() {
        SIZE = 11;
        givenBoardWithMeatChopper(SIZE);

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼        &☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        dice(meatChppperDice, 1, Direction.DOWN.value());
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        dice(meatChppperDice, 1, Direction.LEFT.value());
        field.tick();
        field.tick();
        hero.act();
        hero.up();
        field.tick();
        hero.up();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼1 &      ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼҉☼ ☼ ☼ ☼ ☼\n" +
                "☼҉x       ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");

        dice(meatChppperDice, SIZE - 2, SIZE - 2, Direction.DOWN.value());
        field.tick();

        asrtBrd("☼☼☼☼☼☼☼☼☼☼☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼&☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼☺        ☼\n" +
                "☼ ☼ ☼ ☼ ☼ ☼\n" +
                "☼         ☼\n" +
                "☼☼☼☼☼☼☼☼☼☼☼\n");
    }

    @Test
    public void shouldFireEventWhenKillBomberman() {
        shouldKillBoomberman_whenBombExploded();

        verify(listener).event(Events.KILL_BOMBERMAN);
    }

    @Test
    public void shouldNoEventsWhenBombermanNotMove() {
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        verifyNoMoreInteractions(listener);
    }

    @Test
    public void shouldFireEventWhenKillWall() {
        givenBoardWithDestroyWallsAt(0, 0);

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "#☺   \n");

        hero.act();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " ҉   \n" +
                "H҉҉ ☺\n");

        verify(listener).event(Events.KILL_DESTROY_WALL);
    }

    private void givenBoardWithDestroyWallsAt(int x, int y) {
        withWalls(new DestroyWallAt(x, y, new WallsImpl()));
        givenBoard(SIZE);
    }

    @Test
    public void shouldFireEventWhenKillMeatChopper() {
        givenBoardWithMeatChopperAt(0, 0);

        hero.act();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " ҉   \n" +
                "x҉҉ ☺\n");

        verify(listener).event(Events.KILL_MEAT_CHOPPER);
    }

    private void givenBoardWithMeatChopperAt(int x, int y) {
        withWalls(new MeatChopperAt(x, y, new WallsImpl()));
        givenBoard(SIZE);
    }

    @Test
    public void shouldCalculateMeatChoppersAndWallKills() {
        withWalls(new MeatChopperAt(0, 0, new DestroyWallAt(0, 1, new MeatChopperAt(0, 2, new DestroyWallAt(0, 3, new WallsImpl())))));
        givenBoard(SIZE);
        canDropBombs(4);
        bombsPower(1);

        asrtBrd("     \n" +
                "#    \n" +
                "&    \n" +
                "#    \n" +
                "&☺   \n");

        hero.act();
        hero.up();
        field.tick();

        hero.act();
        hero.up();
        field.tick();

        hero.act();
        hero.up();
        field.tick();

        hero.act();
        hero.up();
        field.tick();
        asrtBrd(" ☺   \n" +
                "#4   \n" +
                "&3   \n" +
                "#2   \n" +
                "&1   \n");

        hero.right();
        field.tick();
        asrtBrd("  ☺  \n" +
                "#3   \n" +
                "&2   \n" +
                "#1   \n" +
                "x҉҉  \n");

        field.tick();
        asrtBrd("  ☺  \n" +
                "#2   \n" +
                "&1   \n" +
                "H҉҉  \n" +
                "&҉   \n");

        field.tick();
        asrtBrd("  ☺  \n" +
                "#1   \n" +
                "x҉҉  \n" +
                "#҉   \n" +
                "&    \n");

        field.tick();
        asrtBrd(" ҉☺  \n" +
                "H҉҉  \n" +
                "&҉   \n" +
                "#    \n" +
                "&    \n");

        hero.left();
        field.tick();
        hero.down();
        hero.act();
        field.tick();
        asrtBrd("     \n" +
                "#☻   \n" +
                "&    \n" +
                "#    \n" +
                "&    \n");
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        asrtBrd(" ҉   \n" +
                "HѠ҉  \n" +
                "&҉   \n" +
                "#    \n" +
                "&    \n");

        assertBombermanDie();

        initBomberman();
        field.tick();
        game.newGame();
        asrtBrd("     \n" +
                "#    \n" +
                "&    \n" +
                "#    \n" +
                "&☺   \n");

        hero.act();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        asrtBrd("     \n" +
                "#    \n" +
                "&    \n" +
                "#҉   \n" +
                "x҉҉☺ \n");
    }

    // если я двинулся за пределы стены и тут же поставил бомбу, то бомба упадет на моем текущем месте
    @Test
    public void shouldMoveOnBoardAndDropBombTogether() {
        givenBoardWithOriginalWalls();
        hero.up();
        field.tick();
        hero.up();
        field.tick();

        hero.left();
        hero.act();
        field.tick();

        asrtBrd("☼☼☼☼☼\n" +
                "☼☻  ☼\n" +
                "☼ ☼ ☼\n" +
                "☼   ☼\n" +
                "☼☼☼☼☼\n");
    }

    // чертик  может ходить по бомбам
    @Test
    public void shouldMonsterCanMoveOnBomb() {
        givenBoardWithMeatChopper(SIZE);

        asrtBrd("☼☼☼☼☼\n" +
                "☼  &☼\n" +
                "☼ ☼ ☼\n" +
                "☼☺  ☼\n" +
                "☼☼☼☼☼\n");

        hero.up();
        field.tick();
        hero.up();
        field.tick();
        hero.right();
        hero.act();
        field.tick();
        hero.left();
        field.tick();
        hero.down();
        field.tick();

        asrtBrd("☼☼☼☼☼\n" +
                "☼ 2&☼\n" +
                "☼☺☼ ☼\n" +
                "☼   ☼\n" +
                "☼☼☼☼☼\n");

        dice(meatChppperDice, 1, Direction.LEFT.value());
        field.tick();

        asrtBrd("☼☼☼☼☼\n" +
                "☼ & ☼\n" +
                "☼☺☼ ☼\n" +
                "☼   ☼\n" +
                "☼☼☼☼☼\n");
    }

    @Test
    public void shouldStopBlastWhenBombermanOrDestroyWalls() {
        bombsPower(5);
        withWalls(new DestroyWallAt(3, 0, new WallsImpl()));
        givenBoard(7);
        when(bombermanDice.next(anyInt())).thenReturn(101); // don't drop perk by accident

        hero.act();
        hero.up();
        field.tick();
        hero.up();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("       \n" +
                "       \n" +
                "       \n" +
                "       \n" +
                "Ѡ      \n" +
                "҉      \n" +
                "҉҉҉H   \n");
    }

    @Test
    public void shouldStopBlastWhenMeatChopper() {
        bombsPower(5);
        withWalls(new MeatChopperAt(4, 0, new WallsImpl()));
        givenBoard(7);

        hero.act();
        hero.up();
        field.tick();
        hero.up();
        field.tick();
        hero.up();
        field.tick();
        hero.right();
        field.tick();
        field.tick();

        asrtBrd("       \n" +
                "҉      \n" +
                "҉      \n" +
                "҉☺     \n" +
                "҉      \n" +
                "҉      \n" +
                "҉҉҉҉x  \n");
    }

    @Test
    public void shouldMeatChopperAppearAfterKill() {
        bombsPower(3);
        dice(meatChppperDice, 3, 0, Direction.DOWN.value());
        withWalls(new MeatChoppers(new WallsImpl(), field, v(1), meatChppperDice));
        givenBoard(SIZE);

        hero.act();
        hero.up();
        field.tick();
        hero.right();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "҉    \n" +
                "҉    \n" +
                "҉☺   \n" +
                "҉҉҉x \n");

        dice(meatChppperDice, 2, 2, Direction.DOWN.value());
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " ☺&  \n" +
                "     \n");
    }

    @Test
    public void shouldMeatChopperNotAppearWhenDestroyWall() {
        bombsPower(3);
        dice(meatChppperDice, 4, 4, Direction.RIGHT.value());
        withWalls(new MeatChoppers(new DestroyWallAt(3, 0, new WallsImpl()), field, v(1), meatChppperDice));
        givenBoard(SIZE);

        hero.act();
        hero.up();
        field.tick();
        hero.right();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("    &\n" +
                "҉    \n" +
                "҉    \n" +
                "҉☺   \n" +
                "҉҉҉H \n");

        dice(meatChppperDice, Direction.DOWN.value());
        field.tick();

        asrtBrd("     \n" +
                "    &\n" +
                "     \n" +
                " ☺   \n" +
                "   # \n");
    }

    // Чертик не может появится на бомбере!
    @Test
    public void shouldMeatChopperNotApperOnBommber() {
        shouldMonsterCanMoveOnBomb();
        hero.down();
        field.tick();

        asrtBrd("☼☼☼☼☼\n" +
                "☼x҉҉☼\n" +
                "☼ ☼ ☼\n" +
                "☼☺  ☼\n" +
                "☼☼☼☼☼\n");

        dice(meatChppperDice, 1, 2, Direction.DOWN.value());
        when(bombermans.contains(anyObject())).thenReturn(true);

        try {
            field.tick();
//            fail(); // TODO надо с этим разобраться
        } catch (IllegalStateException e) {
            assertEquals("java.lang.IllegalStateException: Dead loop at MeatChoppers.regenerate!", e.toString());
        }

        asrtBrd("☼☼☼☼☼\n" +
                "☼   ☼\n" +
                "☼ ☼ ☼\n" +
                "☼☺  ☼\n" +
                "☼☼☼☼☼\n");

    }

    // под разрущающейся стенкой может быть приз - это специальная стенка
    // появляется приз - увеличение длительности ударной волны - его может бомбермен взять и тогда ударная волна будет больше
    // появляется приз - хождение сквозь разрушающиеся стенки - взяв его, бомбермен может ходить через тенки
    // чертики тоже могут ставить бомбы

    // Perks related test here
    @Test
    public void shouldPerkBeDropped_whenWallIsDestroyed() {
        givenBoardWithDestroyWalls(6);
        PerksSettingsWrapper.setPerkSettings(Elements.BOMB_BLAST_RADIUS_INCREASE, 5, 3);
        PerksSettingsWrapper.setDropRatio(20); // 20%
        when(bombermanDice.next(anyInt())).thenReturn(10, 30); // must drop 1 perk

        hero.act();
        field.tick();
        hero.up();
        field.tick();
        hero.up();
        field.tick();
        hero.right();
        field.tick();
        field.tick();

        asrtBrd("######\n" +
                "# # ##\n" +
                "# ☺  #\n" +
                "#҉# ##\n" +
                "+҉҉  #\n" +
                "#H####\n");
    }

    @Test
    public void shouldBombermanAcquirePerk_whenMoveToFieldWithPerk() {
        // BBRI = Bomb Blast Radius Increase perk

        givenBoardWithDestroyWalls(6);
        PerksSettingsWrapper.setPerkSettings(Elements.BOMB_BLAST_RADIUS_INCREASE, 4, 3);
        PerksSettingsWrapper.setDropRatio(20); // 20%
        when(bombermanDice.next(anyInt())).thenReturn(10); // must drop 2 perks
        hero.act();
        field.tick();
        hero.right();
        field.tick();
        hero.right();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("######\n" +
                "# # ##\n" +
                "#    #\n" +
                "#҉# ##\n" +
                "+҉҉☺ #\n" +
                "#+####\n");

        field.tick();

        asrtBrd("######\n" +
                "# # ##\n" +
                "#    #\n" +
                "# # ##\n" +
                "+  ☺ #\n" +
                "#+####\n");

        // go for perk
        hero.left();
        field.tick();
        hero.left();
        field.tick();
        hero.left();
        field.tick();

        asrtBrd("######\n" +
                "# # ##\n" +
                "#    #\n" +
                "# # ##\n" +
                "☺    #\n" +
                "#+####\n");

        assertEquals("Hero had to acquire new perk", 1, player.getHero().getPerks().size());

    }

    @Test
    public void shouldPerkBeDeactivated_whenTimeout() {
        PerksSettingsWrapper.setPerkSettings(Elements.BOMB_BLAST_RADIUS_INCREASE, 4, 3);
        PerksSettingsWrapper.setDropRatio(20);
        player.getHero().addPerk(new BombBlastRadiusIncrease(4,3));
        assertEquals("Hero had to acquire new perk", 1, player.getHero().getPerks().size());

        field.tick();
        field.tick();
        field.tick();

        assertEquals("Hero had to loose perk", 0, player.getHero().getPerks().size());
    }

    @Test
    public void shouldBombBlastRadiusIncrease_whenBBRIperk() {
        givenBoardWithDestroyWalls(6);
        player.getHero().addPerk(new BombBlastRadiusIncrease(4,3));

        hero.act();
        field.tick();
        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("#H####\n" +
                "#҉# ##\n" +
                "#҉   #\n" +
                "#҉# ##\n" +
                "HѠ҉҉҉H\n" +
                "#H####\n");
    }

    @Test
    public void shouldBombCountIncrease_whenBCIperk() {
        // Bomb Count Increase perk
        hero.act();
        // obe bomb by default on lel 1
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "☻    \n");

        hero.right();
        field.tick();
        hero.act();
        // no more bombs :(
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "4☺   \n");

        // add perk that gives 1+3 = 4 player's bombs in total on the board
        player.getHero().addPerk(new BombCountIncrease(3,3));
        hero.act();
        hero.right();
        field.tick();
        hero.act();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "34☻  \n");

        hero.right();
        field.tick();
        hero.act();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "234☻ \n");

        hero.right();
        field.tick();
        hero.act();
        // 4 bombs and no more
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "1234☺\n");

    }

    @Test
    public void shouldHeroKeepAlive_whenBIperk() {
        // Bomb Immune perk
        hero.act();
        hero.right();
        field.tick();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                "4☺   \n");

        player.getHero().addPerk(new BombImmune(6));

        field.tick();
        field.tick();
        field.tick();
        field.tick();

        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "҉    \n" +
                "҉☺   \n");

        hero.act();
        field.tick();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
                " ☻   \n");

        field.tick();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                "     \n" +
//                " 3☺  \n");
                " ☻   \n");

        field.tick();
        field.tick();
        field.tick();
        asrtBrd("     \n" +
                "     \n" +
                "     \n" +
                " ҉   \n" +
                "҉Ѡ҉  \n");
    }

    static class DestroyWallAt extends WallsDecorator {

        public DestroyWallAt(int x, int y, Walls walls) {
            super(walls);
            walls.add(new DestroyWall(x, y));
        }

        @Override
        public Wall destroy(int x, int y) {   // неразрушаемая стенка
            return walls.get(x, y);
        }

    }

    static class MeatChopperAt extends WallsDecorator {

        public MeatChopperAt(int x, int y, Walls walls) {
            super(walls);
            walls.add(new MeatChopper(x, y));
        }

        @Override
        public Wall destroy(int x, int y) {   // неубиваемый монстрик
            return walls.get(x, y);
        }

    }


}
