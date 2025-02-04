package solo.project.service;

import jakarta.servlet.http.HttpServletRequest;

public interface CoupleService {
  void createCouple(String partnerCode, HttpServletRequest request);
  void deleteCouple(HttpServletRequest request);

}
