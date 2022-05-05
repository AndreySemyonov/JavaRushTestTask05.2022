package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class PlayerService {
    @Autowired
    PlayerRepository playerRepository;

    public Specification<Player> setFilterPlayerSpec(String name, String title, Race race, Profession profession, Long after, Long before,
                                                     Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel,
                                                     Integer maxLevel){
        Specification<Player> filter = Specification.where(null);

        if (name != null)
            filter = filter.and(PlayerSpecifications.nameContains(name));
        if (title != null)
            filter = filter.and(PlayerSpecifications.titleContains(title));
        if (race != null)
            filter = filter.and(PlayerSpecifications.raceContains(race));
        if (profession != null)
            filter = filter.and((PlayerSpecifications.professionContains(profession)));
        if (after != null)
            filter = filter.and(PlayerSpecifications.birthdayGreaterThanOrEq(after));
        if (before != null)
            filter = filter.and(PlayerSpecifications.birthdayLessThanOrEq(before));
        if (banned != null)
            filter = filter.and(PlayerSpecifications.equalBanned(banned));
        if (minExperience != null)
            filter = filter.and(PlayerSpecifications.experienceGreaterThanOrEq(minExperience));
        if (maxExperience != null)
            filter = filter.and(PlayerSpecifications.experienceLessThanOrEq(maxExperience));
        if (minLevel != null)
            filter = filter.and(PlayerSpecifications.levelGreaterThanOrEq(minLevel));
        if (maxLevel != null)
            filter = filter.and(PlayerSpecifications.levelLessThanEq(maxLevel));

        return filter;
    }

    public Page<Player> getAll(Specification<Player> specification, PageRequest pageRequest){
        return playerRepository.findAll(specification, pageRequest);
    }

    public Player add(Player player) {
        if (player.getBanned() == null || player.getName().length() > 12 || player.getName().equals("")
            || player.getTitle() == null || player.getTitle().length() > 30
            || player.getRace() == null || player.getProfession() == null
            || player.getBirthday() == null
            || player.getExperience() == null || player.getExperience() > 10000000 || player.getExperience() < 0
            || player.getBirthday().getTime() < 0 || player.getBirthday().before(new Date(100, Calendar.JANUARY, 1))
            || player.getBirthday().after(new Date(1100, Calendar.DECEMBER, 31)))

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        player.setLevel(player.getCurrentLevel());
        player.setUntilNextLevel(player.nextLevel());

        return playerRepository.save(player);
    }

    public Player getById(Long id){
        if (id <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        Player player = playerRepository.findById(id).orElse(null);
        if (player == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return player;
    }

    public ResponseEntity<Player> update (Long id, Player newPlayer){
        if (id <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        Player oldPlayer = getById(id);

        if (newPlayer.getId() == null && newPlayer.getName() == null && newPlayer.getTitle() == null
            && newPlayer.getRace() == null && newPlayer.getProfession() == null && newPlayer.getBirthday() == null
            && newPlayer.getBanned() == null && newPlayer.getExperience() == null && newPlayer.getLevel() == null
            && newPlayer.getUntilNextLevel() == null)
            return new ResponseEntity<>(oldPlayer, HttpStatus.OK);

        if (oldPlayer == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        if (newPlayer.getName()!=null && newPlayer.getName().length()<=12 && newPlayer.getName().length()!=0)
            oldPlayer.setName(newPlayer.getName());
        if (newPlayer.getTitle()!=null && newPlayer.getTitle().length()<=30)
            oldPlayer.setTitle(newPlayer.getTitle());
        if (newPlayer.getRace()!=null)
            oldPlayer.setRace(newPlayer.getRace());
        if (newPlayer.getProfession()!= null)
            oldPlayer.setProfession(newPlayer.getProfession());

        if (newPlayer.getBirthday()!=null){
            if ((newPlayer.getBirthday().getTime() > 0) &&
            !newPlayer.getBirthday().before(new Date(100,Calendar.JANUARY,1)) &&
            !newPlayer.getBirthday().after(new Date(1100, Calendar.DECEMBER, 31))) {
                oldPlayer.setBirthDate(newPlayer.getBirthday());
            }
            else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (newPlayer.getBanned()!=null)
            oldPlayer.setBanned(newPlayer.getBanned());
        if (newPlayer.getExperience()!=null){
            if (newPlayer.getExperience() <= 10000000 && newPlayer.getExperience() >= 0){
                oldPlayer.setExperience(newPlayer.getExperience());
                oldPlayer.setLevel(newPlayer.getCurrentLevel());
                oldPlayer.setUntilNextLevel(newPlayer.nextLevel());
            }
            else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(playerRepository.save(oldPlayer),HttpStatus.OK);
    }

    public ResponseEntity<Player> delete (Long id) {
        if (id <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if(!playerRepository.findById(id).isPresent())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        playerRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }



}
