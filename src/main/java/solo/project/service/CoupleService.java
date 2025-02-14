package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;
import solo.project.entity.Couple;

public interface CoupleService {
  void createCouple(String partnerCode, HttpServletRequest request);
  void deleteCouple(HttpServletRequest request);
  Couple getCoupleDetails(HttpServletRequest request);
}
