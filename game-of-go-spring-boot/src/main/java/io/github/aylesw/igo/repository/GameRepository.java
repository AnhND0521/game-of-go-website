package io.github.aylesw.igo.repository;

import io.github.aylesw.igo.entity.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {
    @Query(value = "{ $or : [ { 'blackPlayer' : ?0 }, { 'whitePlayer' : ?0 } ] }",
            sort = "{ 'time' : -1 }")
    List<Game> findGamesByAccountId(String id);

    @Query(value = "{ $or : [ { 'blackPlayer' : ?0 }, { 'whitePlayer' : ?0 } ] }",
            count = true)
    int countGamesByAccountId(String id);

    @Query(value = "{ $or : [ { $and : [ { 'blackPlayer' : ?0 }, { $expr : { $gt : ['$blackScore', '$whiteScore'] } } ] }, " +
            "{ $and : [ { 'whitePlayer' : ?0 }, { $expr : { $gt : ['$whiteScore', '$blackScore'] } } ] } ] }",
            count = true)
    int countWinsByAccountId(String id);

    @Query(value = "{ $and : [ { $or : [ { 'blackPlayer' : ?0 }, { 'whitePlayer' : ?0 } ] }, " +
            "{ 'blackScore' : { $eq : '$whiteScore' } } ] }",
            count = true)
    int countDrawsByAccountId(String id);
}
