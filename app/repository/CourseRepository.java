package repository;

import models.Courses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class CourseRepository {

    private final JPAApi jpaApi;

    private static final Logger log = LoggerFactory.getLogger(CourseRepository.class);

    public CourseRepository(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    public boolean save(Courses course){
        try{
                jpaApi.withTransaction(entityManager -> {
                entityManager.persist(course);
            });
                log.info("save successful for course {}", course.getCourseCode());
                return true;
        }catch (Exception e) {
                log.error("save failed for course {} with exception {}", course.getCourseCode(), e.getMessage());
                return false;
        }
    }


    public Optional<Courses> findByCourseCode(String courseCode){
        try{
            return jpaApi.withTransaction(entityManager -> {
                Courses course = entityManager.find(Courses.class, courseCode);
                if(course == null){
                    log.error("course {} not found", courseCode);
                    return Optional.empty();
                }
                else{
                    log.info("course {} found", course.getCourseCode());
                    return Optional.of(course);
                }
            });
    } catch (Exception e) {
        log.error("findByCourseCode failed for course {} with exception {}", courseCode, e.getMessage());
        return Optional.empty();
    }
  }
}
