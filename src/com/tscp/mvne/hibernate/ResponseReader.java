package com.tscp.mvne.hibernate;

import java.util.List;

import com.tscp.mvne.exception.DaoException;

public class ResponseReader {

	public static void validateSuccess(List<GeneralSPResponse> responseHolder) throws DaoException {
		isSuccess(responseHolder);
	}

	public static boolean isSuccess(List<GeneralSPResponse> responseHolder) throws DaoException {
		if (responseHolder != null) {
			GeneralSPResponse response = responseHolder.get(0);
			String status = response.getStatus();
			if (status != null && response.getStatus().equals("Y")) {
				return true;
			} else {
				throw new DaoException(response.getMsg());
			}
		} else {
			throw new DaoException("No response received");
		}
	}

	public static int getValue(List<GeneralSPResponse> responseList) {
		return responseList.get(0).getCode();
	}

	public static String getMessage(List<GeneralSPResponse> responseList) {
		return responseList.get(0).getMsg();
	}

}
